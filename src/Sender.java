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
import java.util.Timer;
import java.util.TimerTask;

public class Sender {

	public static final String CRC_CCITT = "10001000000100001";
	public static final int WINDOW_SIZE = 7;
	public static final int MAX_NUM = 7;
	public static final int TIMEOUT_DELAY = 3000;

	private LinkedList<CharFrame> sentFrames;
	private int nextFrameNum;

	private String hostName;
	private int portNumber;

	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;

	/**
	 * @param hostName   host name
	 * @param portNumber port number
	 */
	public Sender(String hostName, int portNumber) throws UnknownHostException, IOException {
		sentFrames = new LinkedList<>();
		nextFrameNum = 0;

		this.hostName = hostName;
		this.portNumber = portNumber;

		socket = new Socket(hostName, portNumber);
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

	}

	/**
	 * Sends a connection request every 3 seconds to the Receiver until it replies
	 * 
	 * @return if succeeded
	 */
	private boolean connect() {

		try {
			CharFrame receptionFrame = new CharFrame('C', "", CRC_CCITT);
			receptionFrame.setNum(nextFrameNum);
			System.out.println("Sending connection request to " + hostName + " at port " + portNumber);
			out.println(receptionFrame.format());

			// Set timeout
			// Set timeout
			TimerTask task = new TimerTask() {
				public void run() {
					CharFrame req = new CharFrame('C', "", CRC_CCITT);
					req.setNum(0);

					synchronized (out) {
						System.out.println("Sending connection request");
						out.println(req.format());
					}
				}
			};
			Timer timeout = new Timer();
			timeout.schedule(task, TIMEOUT_DELAY, TIMEOUT_DELAY);

			String receptionFrameString = in.readLine();
			timeout.cancel();
			receptionFrame = new CharFrame(receptionFrameString, CRC_CCITT);

			if (receptionFrame.getType() != 'A') {
				System.out.println("Connection request rejected");
				return false;
			}
		} catch (InvalidFrameException e) {
			System.out.println("Sender received invalid frame");
		} catch (IOException e) {
			System.out.println("IOException in Sender.connect()");
			return false;
		}

		System.out.println("Connection established");
		nextFrameNum = (nextFrameNum + 1) % MAX_NUM;
		return true;
	}

	/**
	 * Waits for the reply of the Receiver and sends a poll
	 * if it didn't receive the reply in 3 seconds
	 */
	private void awaitRR() {
		String receivedFrameString;

		try {
			// Set timeout
			TimerTask task = new TimerTask() {
				public void run() {
					CharFrame conn = new CharFrame('P', "", CRC_CCITT);
					conn.setNum(0);

					synchronized (out) {
						System.out.println("Sending poll");
						out.println(conn.format());
					}
				}
			};
			Timer timeout = new Timer();
			timeout.schedule(task, TIMEOUT_DELAY, TIMEOUT_DELAY);

			receivedFrameString = in.readLine();
			task.cancel();
			CharFrame receptionFrame = new CharFrame(receivedFrameString, CRC_CCITT);

			System.out.println("Received " + receptionFrame.getType() + " for no." + receptionFrame.getNum());

			for (Iterator<CharFrame> it = sentFrames.iterator(); it.hasNext();) {
				CharFrame f = it.next();
				// if current frame is concerned by received frame
				if ((receptionFrame.getNum() <= nextFrameNum
						&& (f.getNum() < receptionFrame.getNum() || f.getNum() > nextFrameNum))
						|| (receptionFrame.getNum() > nextFrameNum
								&& (f.getNum() > nextFrameNum && f.getNum() < receptionFrame.getNum()))) {

					it.remove();
					System.out.println("no." + f.getNum() + " acknowledged");
				} else {
					if (receptionFrame.getType() == 'A') {
						break;
					}

					// else type is 'R'
					System.out.println("Resent no." + f.getNum() + ": \"" + f.getData() + "\"");
					out.println(f.format());
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
	 * 
	 * @param filename name of the file to be read
	 */
	public void send(String filename) throws FileNotFoundException {

		try {
			// open connection
			// wait for confirmation
			while (!connect()) {
			}

			// send data
			FrameFileReader ffr = new FrameFileReader(filename, CRC_CCITT);

			CharFrame sendFrame = ffr.getNextFrame();

			while (sendFrame != null) {
				// send frames until window is full or EOF
				while (sentFrames.size() < WINDOW_SIZE && sendFrame != null) {
					// set frame num
					sendFrame.setNum(nextFrameNum);
					nextFrameNum = (nextFrameNum + 1) % (MAX_NUM + 1);

					// write a frame and add it to the sentFrames list
					System.out.println("Sent no." + sendFrame.getNum() + ": \"" + sendFrame.getData() + "\"");
					out.println(sendFrame.format());
					sentFrames.add(sendFrame);

					sendFrame = ffr.getNextFrame();
				}
				// wait for rr or rej
				awaitRR();
			}

			while (sentFrames.size() > 0) {
				// wait for rr or rej
				awaitRR();
			}

			// close connection
			while (!close()) {
			}

		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			System.out.println("IOException in Sender.send()");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Closes the file
	 * @return if succeeded
	 * @throws IOException
	 */
	public boolean close() throws IOException {
		Timer timeout = new Timer();

		try {
			CharFrame sendFrame = new CharFrame('F', "", CRC_CCITT);
			sendFrame.setNum(nextFrameNum);
			System.out.println("Sending closing request no." + sendFrame.getNum());
			out.println(sendFrame.format());

			// Set timeout
			TimerTask task = new TimerTask() {
				public void run() {
					CharFrame req = new CharFrame('F', "", CRC_CCITT);
					req.setNum(nextFrameNum);

					synchronized (out) {
						out.println(req.format());
					}
				}
			};
			timeout.schedule(task, TIMEOUT_DELAY, TIMEOUT_DELAY);

			String receptionFrameString = in.readLine();
			timeout.cancel();
			CharFrame receptionFrame = new CharFrame(receptionFrameString, CRC_CCITT);

			if (receptionFrame.getType() != 'A') {
				System.out.println("Closing request rejected");
				return false;
			}
		} catch (InvalidFrameException e) {
			System.out.println("Sender received invalid frame");
		} catch (IOException | NullPointerException e) {
			System.out.println("Receiver already closed");
		}
		
		timeout.cancel();
		socket.close();
		System.out.println("Closed connection");
		return true;
	}
}
