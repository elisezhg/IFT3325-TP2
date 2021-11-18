package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {
    private int portNumber;

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
            String str = in.readLine();  
            System.out.println("echo " + str);
            
            String frame;

            /**
             * TODO:
             * - unstuff frame
             * - checksum
             * - send back appropriate reply
             */
            while ((frame = in.readLine()) != null) {
                System.out.println(frame);
                // out.println(frame);
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
