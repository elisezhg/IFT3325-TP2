package src;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Test {
    public static final int TEST_PORT = 50000;
    public static final int REC_PORT = 50001;
    public static final String PRINT_PADDING = "\t\t\t";

    public static void runTests() {
        System.out.println("Running Tests :");

        /*
         * TESTS:
         *
         * -Frame with errors from Sender
         * -Frame with errors from Receiver
         * -Frame lost from Sender
         * -Frame lost from Receiver
         * -send enough frames to make the window go all the way around
         *
         */

        try {
            // open serverSockets
            ServerSocket serverSocket = new ServerSocket(TEST_PORT);
            Receiver receiver = new Receiver(REC_PORT);

            // start receiver
            Thread receiverThread = new Thread(new Runnable() {
                public void run() {
                    receiver.listen();
                }
            });
            receiverThread.start();

            // start sender
            Sender sender = new Sender("localhost", TEST_PORT);

            String filename = "test/foo.txt";
            Thread senderThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        sender.send(filename);
                    } catch (IOException e) {
                        System.out.println("IOException. Stopping Tests.");
                        return;
                    }
                }
            });

            senderThread.start();

            // let sender's socket connect
            Socket senderSocket = serverSocket.accept();
            serverSocket.close();
            PrintWriter senderOut = new PrintWriter(senderSocket.getOutputStream(), true);
            BufferedReader senderIn = new BufferedReader(new InputStreamReader(senderSocket.getInputStream()));

            // connect to receiver's socket
            Socket receiverSocket = new Socket("localhost", REC_PORT);
            PrintWriter recOut = new PrintWriter(receiverSocket.getOutputStream(), true);
            BufferedReader recIn = new BufferedReader(new InputStreamReader(receiverSocket.getInputStream()));

            // start a thread to mess with sender -> receiver transmissions
            Thread sToR = new Thread(new Runnable() {
                public void run() {
                    int counter = 0;
                    String m;
                    while (true) {
                        try {
                            while ((m = senderIn.readLine()) != null) {
                                // TODO:tests

                                // Frame with errors from Sender
                                if (counter == 4) {
                                    System.out.println(PRINT_PADDING + "S to R: Corrupted frame");
                                    m = randError(m);

                                    // Frame lost from Sender
                                } else if (counter == 2) {
                                    System.out.println(PRINT_PADDING + "S to R: Frame lost");
                                    counter++;
                                    continue;

                                    // Frame delayed
                                } else if (counter == 6) {
                                    System.out.println(PRINT_PADDING + "S to R: Frame delayed");
                                    Thread.sleep(7000);
                                }

                                recOut.println(m);
                                counter++;
                            }
                        } catch (IOException e) {
                            System.out.println("IOException in Sender to Receiver transmission. Stopping tests");
                            return;
                        } catch (InterruptedException e) {
                            System.out.println(e);
                            return;
                        }
                    }
                }
            });
            sToR.start();

            // start a thread to mess with receiver -> sender transmissions
            Thread rToS = new Thread(new Runnable() {
                int counter = 0;

                public void run() {
                    String m;
                    while (true) {
                        try {
                            while ((m = recIn.readLine()) != null) {
                                // TODO:tests

                                // Frame with errors from Receiver
                                if (counter == 3) {
                                    System.out.println(PRINT_PADDING + "R to S: Corrupted frame");
                                    m = randError(m);

                                    // Frame lost from Receiver
                                } else if (counter == 5) {
                                    System.out.println(PRINT_PADDING + "R to S: Frame lost");
                                    counter++;
                                    continue;

                                    // Frame delayed
                                } else if (counter == 6) {
                                    System.out.println(PRINT_PADDING + "R to S: Frame delayed");
                                    Thread.sleep(7000);
                                }

                                senderOut.println(m);
                                counter++;
                            }
                        } catch (IOException e) {
                            System.out.println("IOException in Sender to Receiver transmission. Stopping tests");
                            return;
                        } catch (InterruptedException e) {
                            System.out.println(e);
                            return;
                        }
                    }
                }

            });
            rToS.start();

            sToR.join();
            rToS.join();

            receiver.close();
            sender.close();
            senderSocket.close();
            receiverSocket.close();
        } catch (IOException e) {
            System.out.println("IOException. Stopping tests.");
        } catch (InterruptedException e) {
            System.out.println("Thread error in tests. Stopping tests.");
            e.printStackTrace();
        }

    }

    public static String randError(String s) {
        StringBuilder result = new StringBuilder(s);
        Random rand = new Random();
        int errStart = rand.nextInt(s.length());
        // errStart + errEnd clamped down to 16 to garantee detection
        int errEnd = errStart + rand.nextInt(Math.min(17, (s.length() - errStart) + 1));

        // flip bits
        for (int i = errStart; i < errEnd; i++)
            if (result.charAt(i) == '0')
                result.setCharAt(i, '1');
            else
                result.setCharAt(i, '0');

        return result.toString();
    }
}
