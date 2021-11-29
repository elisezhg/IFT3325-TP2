package src;

import java.io.PrintWriter;
import java.util.TimerTask;

public class ClosingTask extends TimerTask {
    private PrintWriter out;
    private String polynomial;
    private int num;

    public ClosingTask(PrintWriter out, String polynomial, int num) {
        this.out = out;
        this.num = num;
        this.polynomial = polynomial;
    }

    public void run() {
        CharFrame conn = new CharFrame('F', "", polynomial);
        conn.setNum(num);

        synchronized (out) {
            System.out.println("Sending closing request");
            out.println(conn.format());
        }
    }
}