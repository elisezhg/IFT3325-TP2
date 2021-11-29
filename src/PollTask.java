package src;

import java.io.PrintWriter;
import java.util.TimerTask;

public class PollTask extends TimerTask {
    private PrintWriter out;
    private String polynomial;

    public PollTask(PrintWriter out, String polynomial) {
        this.out = out;
        this.polynomial = polynomial;
    }

    public void run() {
        CharFrame poll = new CharFrame('P', "", polynomial);
        poll.setNum(0);

        synchronized (out) {
            System.out.println("Sending poll");
            out.println(poll.format());
        }
    }
}