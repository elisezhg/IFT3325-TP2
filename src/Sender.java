package src;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimerTask;
import java.util.Timer;

public class Sender {

    public static final String CRC_CCITT = "10001000000100001";
    public static final int  WINDOW_SIZE = 7;
    public static final int MAXNUM = 7;

    public final int TIMEOUT_DELAY = 3000;


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
        }
    }


    LinkedList<CharFrame> sentFrames;
    int nextFrameNum;


    String hostName;
    int portNumber;

    Socket socket;
    PrintWriter out;
    BufferedReader in;

    /**
     * @param hostName host name
     * @param portNumber port number
     */
    public Sender(String hostName, int portNumber) throws UnknownHostException, IOException{
	sentFrames = new LinkedList<>();
	nextFrameNum = 0;

	this.hostName = hostName;
	this.portNumber = portNumber;

	socket = new Socket(hostName, portNumber);
	out = new PrintWriter(socket.getOutputStream(), true);
	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    }

    private boolean connect() {

	try{
	    CharFrame receptionFrame = new CharFrame('C', "", CRC_CCITT);
	    String stringFrame = receptionFrame.format();
	    receptionFrame.setNum(nextFrameNum);
	    System.out.println("Sending connection request to " + hostName +
			       " at port " + portNumber);
	    out.println(stringFrame);

	    //set timout//TODO: give timer to poll too
	    PollTask pollTask = new PollTask(out);
	    Timer timeout = new Timer();
	    timeout.schedule(pollTask, TIMEOUT_DELAY, TIMEOUT_DELAY);

	    receptionFrame = new CharFrame(in.readLine(), CRC_CCITT);
	    timeout.cancel();

	    if (receptionFrame.getType() != 'A') {
		System.out.println("Connection request rejected");
		return false;
	    }
	}catch(InvalidFrameException e){
	    System.out.println("error in Sender.connect()");
	    e.printStackTrace();
	    System.exit(1);
	} catch (IOException e) {
	    System.out.println("IOException in Sender.connect()");
	    return false;
	}

	System.out.println("Connection established");
	nextFrameNum = (nextFrameNum + 1) % MAXNUM;
	return true;
    }

    private void awaitRR() {
	try{
	    String stringFrame;

	    //set timout
	    PollTask pollTask = new PollTask(out);
	    Timer timeout = new Timer();
	    timeout.schedule(pollTask, TIMEOUT_DELAY, TIMEOUT_DELAY);

	    CharFrame receptionFrame = new CharFrame(in.readLine(), CRC_CCITT);
	    pollTask.cancel();

	    System.out.println("received " + receptionFrame.getType() + " " + receptionFrame.getNum());

	    for (Iterator<CharFrame> it = sentFrames.iterator(); it.hasNext();) {
		CharFrame f = it.next();
		//if current frame is concerned by ack
		if((receptionFrame.getNum() <= nextFrameNum
		    && (f.getNum() < receptionFrame.getNum() || f.getNum() > nextFrameNum))
		   ||(receptionFrame.getNum()> nextFrameNum
		      &&(f.getNum()>nextFrameNum && f.getNum() < receptionFrame.getNum()))){

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
	} catch (InvalidFrameException e) {
	    System.out.println("Sender received invalid frame");
	} catch (IOException e) {
	    System.out.println("IOException in Sender.awaitRR()");
	}
    }
    /**
     * Send the data from a file to another machine using a simplified HDLC protocol
     * @param filename name of the file to be read
     */
    public void send(String filename) throws FileNotFoundException{

	try{
	    //open connection
	    //wait for confirmation
	    while (!connect()) {}

	    //send data
	    FrameFileReader ffr = new FrameFileReader(filename, CRC_CCITT);

	    CharFrame sendFrame = ffr.getNextFrame();
	    String stringFrame;

	    while (sendFrame != null) {
		//send frames until window is full
		while (sentFrames.size() < WINDOW_SIZE && sendFrame != null) {
		    //set frame num
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

		awaitRR();

	    }
	    while (sentFrames.size() > 0) {
		//wait for ack or rej
		awaitRR();
	    }
	    //close connection
	    sendFrame = new CharFrame('F', "", CRC_CCITT);
	    sendFrame.setNum(nextFrameNum);
	    System.out.println("sending closing request no."  +sendFrame.getNum());
	    out.println(sendFrame.format());
	    System.out.println("Closed connection");

	}catch(FileNotFoundException e){
	    throw e;
	}catch (IOException e) {
	    System.out.println("IOException in Sender.send()");
	} catch(InvalidFrameException e){
	    System.out.println("Trying to format invalid frame in Sender.send()");
	}

    }

    public void close(){
	try{
	    socket.close();
	} catch (IOException e) {
	    System.out.println("IOException closing Sender socket");
	}
    }
}
