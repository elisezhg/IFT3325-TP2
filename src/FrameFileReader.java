package src;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FrameFileReader {
    private BufferedReader reader;
    private String polynomial;

    public FrameFileReader(String filename, String polynomial) throws FileNotFoundException {
        reader = new BufferedReader(new FileReader(filename));
        this.polynomial = polynomial;
    }

    /**
     * reads a frame from open file.
     *
     * @return read frame
     */
    public CharFrame getNextFrame() throws IOException {
        String line = reader.readLine();

        // EOF
        if (line == null) {
            reader.close();
            return null;
        }

        return new CharFrame('I', line, polynomial);

    }
}
