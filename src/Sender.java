package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Sender {
    /**
     * Send the data from a file to another machine
     * https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/networking/sockets/examples/EchoClient.java
     * @param hostName host name
     * @param portNumber port number
     * @param filename name of the file to be read
     */
    public void send(String hostName, int portNumber, String filename) {
        try {
            Socket echoSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

            // for debugging purposes
            out.println("client connected");

            FrameFileReader ffr = new FrameFileReader(filename);
            CharFrame frame;

            /**
             * TODO:
             * - add a time out
             * - read the receipt from the receiver
             */
            while ((frame = ffr.getNextFrame_()) != null) {
                out.println(frame.format());
                // System.out.println("echo: " + in.readLine());
            }

            echoSocket.close();

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);

        } catch (IOException e) {
            System.out.println(e);
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
