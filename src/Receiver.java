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

    private PrintWriter out;
    private BufferedReader in;


    /**
     * Listen to the port number
     * https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoServer.java
     * @param portNumber
     */
    public void listen(int portNumber) {
        try {
            while (true) {
                ServerSocket serverSocket = new ServerSocket(portNumber);
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

                    Boolean isNextFrame = expectedFrameNum == receivedFrameNum;

                    // TOD: TO BE REMOVED
                    // for debugging purposes (skips frame 3)
                    // if (receivedFrameNum == 3 && !skipped) {
                    //     System.out.println("skipping no." + receivedFrameNum);
                    //     skipped = true;
                    //     continue;
                    // }

                    // Check validity of the frame
                    if (receivedFrame.isValid() && isNextFrame) {
                    
                        // Establish connection if not done yet
                        if (!connected && receivedFrameType == 'C') connected = true;
                        
                        if (connected) {
                            System.out.println("Received no." + receivedFrameNum + ": "  + receivedFrameData);
                            
                            // Send reception receipt (RR)
                            sendReceipt('A', expectedFrameNum);
                            
                            // Set last received number to current frame number
                            expectedFrameNum =  (receivedFrameNum + 1) % WINDOW_SIZE;

                            // If frame demands to end connection
                            if (receivedFrameType == 'F') {
                                connected = false;
                                serverSocket.close();
                                System.out.println("Ending connection.");
                                break;
                            }
                        
                        // Not connected
                        } else {
                            sendReceipt('R', expectedFrameNum);
                        }

                    // Invalid frame
                    } else {
                        sendReceipt('R', expectedFrameNum);
                    }
                }

            }
        
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
               + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void sendReceipt(char type, int num) throws InvalidFrameException {
        CharFrame receipt = new CharFrame(type, "", CRC_CCITT);
        receipt.setNum(num);
        this.out.println(receipt.format());

        if (type == 'A') {
            System.out.println("Sending ACK for no." + num);
        } else {
            System.out.println("Sending REJ for no." + num);
        }
    }
}
