package src;

import java.io.PrintWriter;
import java.util.TimerTask;

public class ConnectionTask extends TimerTask {
    private PrintWriter out;
    private String polynomial;

    public ConnectionTask(PrintWriter out, String polynomial) {
        this.out = out;
        this.polynomial = polynomial;
    }

    public void run() {
        CharFrame conn = new CharFrame('C', "", polynomial);
        conn.setNum(0);

        synchronized (out) {
            System.out.println("Sending connection request");
            out.println(conn.format());
        }
    }
}