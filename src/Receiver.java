package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {
    public static final String CRC_CCITT = "10001000000100001";
    public static final int WINDOW_SIZE = 7;
    public static final int MAX_NUM = 7;

    private boolean closed;

    private ServerSocket serverSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Socket clientSocket;

    public Receiver(int portNumber) throws IOException {
        closed = false;
        this.serverSocket = new ServerSocket(portNumber);
    }

    public void close() {
        try {
            closed = true;
            serverSocket.close();
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("IOException closing Receiver side sockets");
            e.printStackTrace();
        }
    }

    /**
     * Listen to the port number
     * https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoServer.java
     * 
     * @param portNumber
     */
    public void listen() {
        try {

            clientSocket = serverSocket.accept();
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

            // loop until close is called
            while (!closed) {
                
                try {
                    // Read and construct received frame
                    receivedFrameString = this.in.readLine();
                    receivedFrame = new CharFrame(receivedFrameString, CRC_CCITT);
    
                    // Get data from frame
                    receivedFrameNum = receivedFrame.getNum();
                    receivedFrameType = receivedFrame.getType();
                    receivedFrameData = receivedFrame.getData();
    
                    isNextFrame = expectedFrameNum == receivedFrameNum;
    
                    if (receivedFrame.getType() == 'P') {
                        System.out.println("Received poll");
                        sendReceipt('A', expectedFrameNum);
                    }

                    if (isNextFrame) {

                        // Establish connection if not done yet
                        if (!connected && receivedFrameType == 'C')
                            connected = true;

                        if (connected) {
                            System.out.println(
                                "Received no." + receivedFrameNum + " [" + receivedFrameType + "]: \"" +
                                receivedFrameData + "\"" 
                            );

                            isWaitingForResend = false;

                            // If frame demands to end connection
                            if (receivedFrameType == 'F') {
                                close();
                                System.out.println("Ending connection.");
                                break;
                            }

                            // Set last received number to current frame number
                            expectedFrameNum = (receivedFrameNum + 1) % (MAX_NUM + 1);

                            // Send reception receipt (RR)
                            sendReceipt('A', expectedFrameNum);

                            // Not connected
                        } else {
                            sendReceipt('R', expectedFrameNum);
                        }

                    // Not the expected frame
                    } else {
                        System.out.println(
                                "Invalid frame: received no." + receivedFrameNum + "\nexpected no." + expectedFrameNum);

                        if (!isWaitingForResend) {
                            sendReceipt('R', expectedFrameNum);
                            isWaitingForResend = true;
                        }
                    }
                } catch (InvalidFrameException e) {
                    System.out.println("Corrupted frame: ignored");
                }
            }
            
            expectedFrameNum = 0;

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
