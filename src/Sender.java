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

                    receptionFrame = new CharFrame(in.readLine(), CRC_CCITT);
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
                    System.out.println("Sent no " + sendFrame.getNum() + " : " + stringFrame);

                    sendFrame = ffr.getNextFrame();
                }
                //wait for ack or rej
                receptionFrame = new CharFrame(in.readLine(), CRC_CCITT);
		System.out.println("received ACK");
                for (Iterator<CharFrame> it = sentFrames.iterator(); it.hasNext();) {
                    CharFrame f = it.next();
                    //if current frame is concerned by ack
		    System.out.println("i is " + f.getNum());
		    System.out.println("n is " + nextFrameNum);
		    System.out.println("A is " + receptionFrame.getNum());
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
                        System.out.println("Resent no " + f.getNum() +" : " + stringFrame);
                    }
                }

	    }
	    while (sentFrames.size() > 0) {
                //wait for ack or rej
                receptionFrame = new CharFrame(in.readLine(), CRC_CCITT);
		System.out.println("received ACK");
                for (Iterator<CharFrame> it = sentFrames.iterator(); it.hasNext();) {
                    CharFrame f = it.next();
                    //if current frame is concerned by ack
		    System.out.println("i is " + f.getNum());
		    System.out.println("n is " + nextFrameNum);
		    System.out.println("A is " + receptionFrame.getNum());
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
                        System.out.println("Resent no " + f.getNum() +" : " + stringFrame);
                    }
                }
	    }
            //close connection
            sendFrame = new CharFrame('F', "", CRC_CCITT);
            sendFrame.setNum(nextFrameNum);
            out.println(sendFrame.format());
            System.out.println("Closed connection");

            socket.close();

        }catch(InvalidFrameException e){
            e.printStackTrace();
        }
    }


    // public void send0(String hostName, int portNumber, String filename) throws UnknownHostException, IOException {
    //     try{
    //         Socket socket = new Socket(hostName, portNumber);
    //         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    //         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    //         Boolean connected = false;

    //         CharFrame sendFrame = null;
    //         char sendFrameType;
    //         int currentFrameNum = 0;
    //         int nextFrameNum = 0;

    //         FrameFileReader ffr = new FrameFileReader(filename, CRC_CCITT);

    //         while (true) {

    //             // Send connection request
    //             if (!connected) {
    //                 sendFrame = new CharFrame('C', "", CRC_CCITT);
    //                 System.out.println("Sending connection request to " + hostName + " at port " + portNumber);

    //             // Get next frame from the file reader
    //             } else {

    //                 // Get next frame only if nextFrameNum has been incremented
    //                 // Else sendFrame is the same as last iteration (i.e. we resend)
    //                 if (currentFrameNum != nextFrameNum) {
    //                     sendFrame = ffr.getNextFrame();
    //                     currentFrameNum = nextFrameNum;
    //                 }

    //                 // Reached EOF: send ending request
    //                 if (sendFrame == null) {
    //                     sendFrame = new CharFrame('F', "", CRC_CCITT);
    //                 }

    //                 System.out.println("Sending no." + currentFrameNum + ": " + sendFrame.getData());
    //             }

    //             sendFrame.setNum(currentFrameNum);
    //             sendFrameType = sendFrame.getType();

    //             // Send frame
    //             out.println(sendFrame.format());

    //             ExecutorService executor = Executors.newSingleThreadExecutor();
    //             Future<String> future = executor.submit(new Callable<String>(){
    //                 public String call() throws Exception {
    //                     return in.readLine();
    //                 }
    //             });

    //             //TODO: bug when timeout
    //             try {
    //                 // System.out.println("Waiting for confirmation...");
    //                 String receivedFrameString = future.get(TIMEOUT_DELAY, TimeUnit.SECONDS);
    //                 // System.out.println("Confirmation received");

    //                 CharFrame receivedFrame = new CharFrame(receivedFrameString, CRC_CCITT);

    //                 // Check validity
    //                 if ((receivedFrame.isValid()) && (receivedFrame.getNum() == currentFrameNum)) {
    //                     System.out.println("currentFrameNum: "+currentFrameNum);
    //                     // ACK
    //                     if (receivedFrame.getType() == 'A') {
    //                         // Increment next frame number
    //                         nextFrameNum = (currentFrameNum + 1) % WINDOW_SIZE;

    //                         // Received confirmation for establishing connection
    //                         if (sendFrameType == 'C') {
    //                             connected = true;
    //                             System.out.println("Connection established");
    //                         }

    //                         // Received confirmation for ending connection
    //                         if (sendFrameType == 'F') {
    //                             socket.close();
    //                             System.out.println("Connection ended.");
    //                             return;
    //                         }

    //                     // REJ
    //                     } else {
    //                         System.out.println("REJ: frame no." + currentFrameNum + " needs to be resent.");
    //                     }
    //                 }

    //             // Didn't receive confirmation on time
    //             } catch (TimeoutException e) {
    //                 future.cancel(true);
    //                 System.out.println("Timeout: frame no." + currentFrameNum + " needs to be resent.");
    //             }
    //         }

    //     } catch (Exception e) {
    //         System.out.println(e);
    //     }
    // }
}
