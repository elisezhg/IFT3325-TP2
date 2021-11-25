package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;


public class Sender {

    public static final String CRC_CCITT = "10001000000100001";
    public static final int  windowSize = 7;
    public static final int maxNum = 7;

    LinkedList<CharFrame> sentFrames;
    int nextFrameNum;

    public Sender() {
	sentFrames = new LinkedList<>();
	nextFrameNum = 0;
    }
    /**
     * Send the data from a file to another machine using a simplified HDLC protocol
     * @param hostName host name
     * @param portNumber port number
     * @param filename name of the file to be read
     */
    public void send(String hostName, int portNumber, String filename) throws UnknownHostException, IOException {
	//TODO: divide into multiple functions : (e.g. openConnection, sendData, sendFrame, awaitRR, etc.)
	//maybe socket and readers and writers should be fields initialised in the constructor
	try{
            Socket socket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          /*
	   * TODO:
	   * - add a time out (add one for connection too)
	   */

	    //open connection
	    CharFrame receptionFrame = new CharFrame('C', "", CRC_CCITT);
	    receptionFrame.setNum(0);
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
		}
	    }

	    //send data
            FrameFileReader ffr = new FrameFileReader(filename, CRC_CCITT);

	    CharFrame sendFrame = ffr.getNextFrame();
            while (sendFrame != null) {
		//send frames until window is full
		while(sentFrames.size() < windowSize && sendFrame != null){//set frame num
		    sendFrame.setNum(nextFrameNum);
		    nextFrameNum = (nextFrameNum + 1) % maxNum;
		    //write a frame and add it to the sentFrames list
		    stringFrame = sendFrame.format();
		    out.println(stringFrame);
		    sentFrames.add(sendFrame);
		    System.out.println("Sent no " + sendFrame.getNum() + " : " + stringFrame);

		    sendFrame = ffr.getNextFrame();
		}
		//wait for ack or rej
		receptionFrame = new CharFrame(in.readLine(), CRC_CCITT);
		for (Iterator<CharFrame> it = sentFrames.iterator(); it.hasNext();) {
		    CharFrame f = it.next();
		    //if current frame is concerned by ack
		    if((f.getNum() > nextFrameNum
			&& receptionFrame.getNum() < nextFrameNum)
		       || (f.getNum() < receptionFrame.getNum()
			   && receptionFrame.getNum() < nextFrameNum)){

			it.remove();
			System.out.println("no " + f.getNum() + " acknowledged");
		    }
		    else {
			if (receptionFrame.getType() == 'A') {
			    it.remove();
			    System.out.println("no " + f.getNum() + " acknowledged");
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

}
