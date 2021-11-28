package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Sender {

    public static final String CRC_CCITT = "10001000000100001";
    public static final int  WINDOW_SIZE = 7;
    public static final int MAXNUM = 7;

    public final int TIMEOUT_DELAY = 3;


    private class PollTask extends TimerTask{
        private PrintWriter out;
        public PollTask(PrintWriter out){
            this.out = out;
        }
        public void run(){
            CharFrame poll = new CharFrame('P', "", CRC_CCITT);
            poll.setNum(0);

            synchronized (out) {
                try{
                    System.out.println("Sending poll");
                    out.println(poll.format());
                } catch (InvalidFrameException e) {
                    System.out.println("An error occured building poll");
                }
            }
            // cancel();
        }
    }


    LinkedList<CharFrame> sentFrames;
    int nextFrameNum;

    public Sender() {
        sentFrames = new LinkedList<>();
        nextFrameNum = 0;
    }
    // /**
    //  * Send the data from a file to another machine using a simplified HDLC protocol
    //  * @param hostName host name
    //  * @param portNumber port number
    //  * @param filename name of the file to be read
    //  */


    public void send(String hostName, int portNumber, String filename) throws UnknownHostException, IOException {
        //TODO: divide into multiple functions : (e.g. openConnection, sendData, sendFrame, awaitRR, etc.)
        //maybe socket and readers and writers should be fields initialised in the constructor
        try{
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //open connection
            CharFrame receptionFrame = new CharFrame('C', "", CRC_CCITT);
            receptionFrame.setNum(nextFrameNum);
            nextFrameNum = (nextFrameNum + 1) % MAXNUM;
            String stringFrame = receptionFrame.format();//stringFrame is used to avoid computing when printing frame
            //wait for confirmation
            while(true){
                try{
                    System.out.println("Sending connection request to " + hostName +
                                       " at port " + portNumber);
                    out.println(stringFrame);

                    //timout
                    PollTask pollTask = new PollTask(out);
                    Timer timeout = new Timer();
                    timeout.schedule(pollTask, 3000);

                    receptionFrame = new CharFrame(in.readLine(), CRC_CCITT);
                    timeout.cancel();

                    if (receptionFrame.getType() != 'A') {
                        System.out.println("Connection request rejected");
                        return;
                    }
                    System.out.println("Connection established");

                    break;
                } catch (InvalidFrameException e) {
                    System.out.println("Received frame containing errors.");
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

            //send data
            FrameFileReader ffr = new FrameFileReader(filename, CRC_CCITT);

            CharFrame sendFrame = ffr.getNextFrame();

            while (sendFrame != null) {
                //send frames until window is full
                while (sentFrames.size() < WINDOW_SIZE && sendFrame != null) {//set frame num
                    sendFrame.setNum(nextFrameNum);
                    nextFrameNum = (nextFrameNum + 1) % (MAXNUM + 1);
                    //write a frame and add it to the sentFrames list
                    stringFrame = sendFrame.format();
                    out.println(stringFrame);
                    sentFrames.add(sendFrame);
                    System.out.println("Sent no " + sendFrame.getNum() + " : " + sendFrame.getData());

                    sendFrame = ffr.getNextFrame();
                }
                //wait for ack or rej
                //timout
                PollTask pollTask = new PollTask(out);
                Timer timeout = new Timer();
                timeout.schedule(pollTask, 3000);

                receptionFrame = new CharFrame(in.readLine(), CRC_CCITT);
                pollTask.cancel();
                System.out.println("received " + receptionFrame.getType() + " " + receptionFrame.getNum());
                for (Iterator<CharFrame> it = sentFrames.iterator(); it.hasNext();) {
                    CharFrame f = it.next();
                    //if current frame is concerned by ack
                    // System.out.println("i is " + f.getNum());
                    // System.out.println("n is " + nextFrameNum);
                    // System.out.println("A is " + receptionFrame.getNum());
                    
                    // (R <= n && (i < R || i > n)) ||(R>n &&(i>n && i < R)

                    // 0 1 2 3 4 5 6] 7
                    // 0 1] 2 [3 4 5 6 7
                    
                    if((receptionFrame.getNum() <= nextFrameNum && (f.getNum() < receptionFrame.getNum() || f.getNum() > nextFrameNum))
                    ||(receptionFrame.getNum()> nextFrameNum &&(f.getNum()>nextFrameNum && f.getNum() < receptionFrame.getNum()))){

                        it.remove();
                        System.out.println("no " + f.getNum() + " acknowledged");
                    }
                    else {
                        if (receptionFrame.getType() == 'A') {
                            break;
                        }
                        //else type is 'R'
                        stringFrame = f.format();
                        out.println(stringFrame);
                        System.out.println("Resent no " + f.getNum() +" : " + f.getData());
                    }
                }

            }
            while (sentFrames.size() > 0) {
                //wait for ack or rej
                //timout
                PollTask pollTask = new PollTask(out);
                Timer timeout = new Timer();
                timeout.schedule(pollTask, 3000);
                
                receptionFrame = new CharFrame(in.readLine(), CRC_CCITT);
                pollTask.cancel();

                System.out.println("received " + receptionFrame.getType() + " " + receptionFrame.getNum());

                for (Iterator<CharFrame> it = sentFrames.iterator(); it.hasNext();) {
                    CharFrame f = it.next();
                    //if current frame is concerned by ack
                    // System.out.println("i is " + f.getNum());
                    // System.out.println("n is " + nextFrameNum);
                    // System.out.println("A is " + receptionFrame.getNum());
                    if((f.getNum() < receptionFrame.getNum())
                       || (receptionFrame.getNum() <= nextFrameNum
                           && f.getNum() > nextFrameNum)){

                        it.remove();
                        System.out.println("no " + f.getNum() + " acknowledged");
                    }
                    else {
                        if (receptionFrame.getType() == 'A') {
                            break;
                        }
                        //else type is 'R'
                        stringFrame = f.format();
                        out.println(stringFrame);
                        System.out.println("Resent no " + f.getNum() +" : " + f.getData());
                    }
                }
            }
            //close connection
            sendFrame = new CharFrame('F', "", CRC_CCITT);
            sendFrame.setNum(nextFrameNum);
            System.out.println("sending closing request no."  +sendFrame.getNum());
            out.println(sendFrame.format());
            System.out.println("Closed connection");

            socket.close();

        }catch(InvalidFrameException e){
            e.printStackTrace();
        }
    }
}
