package src;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FrameFileReader {
    public static int FRAME_ENCODING_BITS = 3;

    private int counter = 0;
    private BufferedReader reader;
    private String polynomial;

    public FrameFileReader(String filename, String polynomial) throws FileNotFoundException{
        reader = new BufferedReader(new FileReader(filename));
	this.polynomial = polynomial;
    }

    public void closeFile() throws IOException{
        reader.close();
    }

    /**
	 * reads a frame from open file.
	 *
	 * @return read frame
	 */
    public CharFrame getNextFrame() throws IOException{
	String line = reader.readLine();

        // EOF
        if (line == null) {
            reader.close();
            return null;
        }

        return new CharFrame('I', line, polynomial);

    }

    // Reads the next line from the open file and constructs a new frame
    public CharFrame getNextFrame_() throws IOException{

        String line = reader.readLine();

        // EOF
        if (line == null) {
            reader.close();
            return null;
        }

        CharFrame frame = new CharFrame('I', line, null);
        this.counter = (this.counter + 1) % (int) Math.pow(2, FRAME_ENCODING_BITS);

        return frame;
    }
}
