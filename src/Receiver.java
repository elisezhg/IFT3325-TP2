package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {
    public static final String CRC_CCITT = "10001000000100001";

    private int lastReceived;

    public Receiver() {
	lastReceived = 0;
    }
    /**
     * Listen to the port number
     * https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoServer.java
     * @param portNumber
     */
    public void listen(int portNumber) {
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // for debugging purposes
            // String str = in.readLine();
            // System.out.println("echo " + str);

            String frame;
	    CharFrame receivedFrame;
	    CharFrame RR;
	    // System.out.println("before unstuffing: " + frame);

	    try {
		receivedFrame = new CharFrame(in.readLine(), CRC_CCITT);
		System.out.println("Received no " + receivedFrame.getNum()
				   + " : " + receivedFrame.format());

		// Send confirmation
		RR = new CharFrame('A', "", CRC_CCITT);
		RR.setNum(receivedFrame.getNum() + 1);
		out.println(RR.format());
		System.out.println("Sending RR: " + RR.format());

	    } catch(InvalidFrameException e) {
		System.out.println("Received invalid frame");
		CharFrame REJ = new CharFrame('R', "", CRC_CCITT);
		REJ.setNum(0); //TODO
		out.println(REJ);

	    } catch (Exception e) {
		System.out.println(e);
	    }

	    //data exchange

	    while(true){
		try{
		    receivedFrame = new CharFrame(in.readLine(), CRC_CCITT);

		    break;
		}catch(InvalidFrameException e){
		    System.out.println("Received invalid frame");
		}
	    }


            serverSocket.close();

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
			       + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }

    }

    private void sendReceipt(String type) {}
}
