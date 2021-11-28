package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {
    public static final String CRC_CCITT = "10001000000100001";
    public static final int  WINDOW_SIZE = 7;
    public static final int MAXNUM = 7;

    private ServerSocket serverSocket;
    private PrintWriter out;
    private BufferedReader in;


    public Receiver(int portNumber) throws IOException{
        this.serverSocket = new ServerSocket(portNumber);
    }

    /**
     * Listen to the port number
     * https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoServer.java
     * @param portNumber
     */
    public void listen() {
        try {

            while (true) {

                Socket clientSocket = serverSocket.accept();
                this.out = new PrintWriter(clientSocket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Init
                Boolean connected = false;
                int expectedFrameNum = 0;

                String receivedFrameString;
                CharFrame receivedFrame;
                char receivedFrameType;
                int receivedFrameNum;
                String receivedFrameData;
                Boolean isNextFrame;
                Boolean isWaitingForResend = false;

                //TODO: to be removed
                Boolean skipped = false;

                // Proceed until sender demands to end
                while (true) {

                    // Read and construct received frame
                    receivedFrameString = this.in.readLine();
                    receivedFrame = new CharFrame(receivedFrameString, CRC_CCITT);

                    // Get data from frame
                    receivedFrameNum = receivedFrame.getNum();
                    receivedFrameType = receivedFrame.getType();
                    receivedFrameData = receivedFrame.getData();

                    isNextFrame = expectedFrameNum == receivedFrameNum;


                    if (receivedFrameNum == 3 && !skipped) {
                        System.out.println("skipping no." + receivedFrameNum);
                        Thread.sleep(4000);
                        skipped = true;
                        continue;
                    }

                    // Check validity of the frame
                    if (receivedFrame.isValid()) {
                        if (receivedFrame.getType() == 'P') {
                            sendReceipt('A', expectedFrameNum);
                            System.out.println("received poll");
                        }

                        if(isNextFrame) {

                            // Establish connection if not done yet
                            if (!connected && receivedFrameType == 'C') connected = true;

                            if (connected) {
                                System.out.println("Received no." + receivedFrameNum + ": "  + receivedFrameData);
                                isWaitingForResend = false;

                                // If frame demands to end connection
                                if (receivedFrameType == 'F') {
                                    connected = false;
                                    serverSocket.close();
                                    System.out.println("Ending connection.");
                                    break;
                                }

                                // Set last received number to current frame number
                                expectedFrameNum = (receivedFrameNum + 1) % (MAXNUM + 1);

                                // Send reception receipt (RR)
                                sendReceipt('A', expectedFrameNum);

                                // Not connected
                            } else {
                                sendReceipt('R', expectedFrameNum);
                            }

                            // Invalid frame
                        } else {
                            System.out.println("Invalid frame: received no." + receivedFrameNum);

                            if (!isWaitingForResend) {
                                sendReceipt('R', expectedFrameNum);
                                isWaitingForResend = true;
                            }
                        }
                    }
                }
                expectedFrameNum = 0;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void sendReceipt(char type, int num) throws InvalidFrameException {
        CharFrame receipt = new CharFrame(type, "", CRC_CCITT);
        receipt.setNum(num);
        this.out.println(receipt.format());

        if (type == 'A') {
            System.out.println("Sending RR for no." + num);
        } else {
            System.out.println("Sending REJ for no." + num);
        }
    }
}
