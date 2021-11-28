package src;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;


public class Test {
    public static final int TEST_PORT = 50000;
    public static final int REC_PORT = 50001;
    public static void runTests() {
	System.out.println("Running Tests :");

	    /*TESTS:
	     *
	     *-Frame with errors from Sender
	     *-Frame with errors from Receiver
	     *-Frame lost from Sender
	     *-Frame lost from Receiver
	     *-send enough frames to make the window go all the way around
	     *
	     */


	    Receiver receiver = new Receiver();
	    Thread receiverThread = new Thread(new Runnable(){
		    public void run() {
			receiver.listen(REC_PORT);
		    }
		});
	    receiverThread.start();

	    Thread testThread = new Thread(new Runnable() {
		    public void run(){
			try{
			    //let sender's socket connect
			    ServerSocket serverSocket = new ServerSocket(TEST_PORT);
			    Socket senderSocket = serverSocket.accept();
			    serverSocket.close();
			    PrintWriter senderOut =
				new PrintWriter(senderSocket.getOutputStream(), true);
			    BufferedReader senderIn = new BufferedReader
				(new InputStreamReader(senderSocket.getInputStream()));

			    //connect to receiver's socket
			    Socket receiverSocket = new Socket("localhost", REC_PORT);
			    PrintWriter recOut =
				new PrintWriter(receiverSocket.getOutputStream(), true);
			    BufferedReader recIn = new BufferedReader
				(new InputStreamReader(receiverSocket.getInputStream()));

			    //start a thread to mess with sender -> receiver transmissions
			    Thread sToR = new Thread(new Runnable() {
				    public void run(){
					String m;
					while (true) {
					    try{
						while ((m = senderIn.readLine()) != null) {
							//TODO:tests
							recOut.println(m);
						}
						senderSocket.close();
						receiverSocket.close();
						return;

					    }catch(IOException e){
						System.out.println("IOException in Sender to Receiver transmission");
					    }
					}
				    }
				});
			    sToR.start();
			    //start a thread to mess with receiver -> sender transmissions
			    Thread rToS = new Thread(new Runnable() {
				    public void run() {
					String m;
					while (true) {
					    try{
						while ((m = recIn.readLine()) != null) {
							//TODO:tests
							senderOut.println(m);
						}
						senderSocket.close();
						receiverSocket.close();
						return;
					    }catch(IOException e){
						System.out.println("IOException in Sender to Receiver transmission");
					    }
					}
				    }

				});
			    rToS.start();

			} catch (IOException e) {
			    System.out.println("IOException. Stopping tests.");
			}
		    }
		});
	    testThread.start();

	    Sender sender = new Sender();
	    //TODO: make runTests() create its own test file and delete it afterward
	    String filename = "test/foo.txt";
	    Thread senderThread = new Thread(new Runnable(){
		    public void run(){
			try{
			    sender.send("localhost", TEST_PORT, filename);
			}catch(UnknownHostException e){
			    System.out.println("UnknownHostException. Stopping tests.");
			    return;
			} catch (IOException e) {
			    System.out.println("IOException. Stopping Tests.");
			    return;
			}
		    }
		});
	    senderThread.start();

    }

    public static String randError(String s) {
	StringBuilder result = new StringBuilder(s);
	Random rand = new Random();
	int errStart = rand.nextInt(s.length());
	//errStart + errEnd clamped down to 16 to garantee detection
	int errEnd = errStart + rand.nextInt(Math.min(17, (s.length() - errStart) + 1));

	//flip bits
	for(int i = errStart; i < errEnd; i++)
	    if(result.charAt(i) == '0')
		result.setCharAt(i, '1');
	    else
		result.setCharAt(i, '0');

	return result.toString();
    }
}
