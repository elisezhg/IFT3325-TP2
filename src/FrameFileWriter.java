package src;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FrameFileWriter {
    BufferedWriter writer;

    public FrameFileWriter() throws IOException {
        String filename = new SimpleDateFormat("'test/out/'yyyyMMddHHmmss'.txt'").format(new Date());
        createOutputFile(filename);
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, true), "UTF-8"));
    }

    /**
     * Creates the output file where we write
     * 
     * @param filename name of the output file
     */
    public void createOutputFile(String filename) throws IOException {
        try {
            File file = new File(filename);
            file.createNewFile();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    /**
     * Writes a frame to the file
     * 
     * @param frame frame to be written
     */
    public void write(CharFrame frame) {
        try {
            writer.write(frame.getData() + "\n");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Closes the writer
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        writer.close();
    }
}
