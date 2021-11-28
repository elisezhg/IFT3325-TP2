package src;

import java.util.Scanner;

public class CLI {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String line = scanner.nextLine();

        while (line != null) {
            String[] params = line.split(" ");
            int lenParams = params.length;

            // Run tests
            if (lenParams == 0) {
                System.out.println("Running tests...");
                Test test = new Test();
                test.runTests();

            // Instantiate Sender
            } else if (params[0].equalsIgnoreCase("sender")) {
                try {
                    System.out.println("sending...");

                    String name = params[1];
                    int port = Integer.parseInt((params[2]));
                    String filename = params[3];
                    Boolean isGoBackN = params[4].equals("0");

                    if (isGoBackN) {
                        Sender sender = new Sender();
                        sender.send(name, port, filename);
                    } else {
                        System.out.println("Protocol not implemented.");
                    }

                //TODO: better error management
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

                //TODO: better error management
                } catch (Exception e) {
                    System.out.println("Please check the arguments.");
                }

            } else {
                System.out.println("Command not recognized.");
            }

            line = scanner.nextLine();
        }

        scanner.close();
        System.exit(0);
    }
}
