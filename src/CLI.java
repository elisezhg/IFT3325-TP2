package src;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class CLI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String line = scanner.nextLine();

        while (line != null) {
	    System.out.print("\n >> ");
            String[] params = line.split(" ");
            if (params.length <= 0)
                continue;
            if (params[0].equalsIgnoreCase("sender")) {
                try {
                    System.out.println("sending...");

                    String name = params[1];
                    int port = Integer.parseInt((params[2]));
                    String filename = params[3];
                    Boolean isGoBackN = params[4].equals("0");

                    if (isGoBackN) {
                        Sender sender = new Sender(name, port);
                        sender.send(filename);
                    } else {
                        System.out.println("Protocol not implemented.");
                    }

                } catch (UnknownHostException e) {
                    System.out.println("Unknown Host");
                } catch (FileNotFoundException e) {
                    System.out.println("File not found.");
                } catch (Exception e) {
                    System.out.println("Please check the arguments.");
                }

                // Instantiate Receiver
            } else if (params[0].equalsIgnoreCase("receiver")) {
                try {
                    System.out.println("listening...");

                    int port = Integer.parseInt((params[1]));

                    Receiver receiver = new Receiver(port);
                    receiver.listen();

                } catch (Exception e) {
                    System.out.println("Please check the arguments.");
                }

            } else if (params[0].equalsIgnoreCase("test")) {
                Test.runTests();
            } else {
                System.out.println("Command not recognized.");
            }

            line = scanner.nextLine();
        }

        scanner.close();
        System.exit(0);
    }
}
