package src;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class FrameFileReader {
    private BufferedReader reader;
    private String polynomial;

    /**
     * Constructor
     * 
     * @param filename   name of the file to be read
     * @param polynomial polynomial of the charframes
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public FrameFileReader(String filename, String polynomial)
            throws FileNotFoundException, UnsupportedEncodingException {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
        this.polynomial = polynomial;
    }

    /**
     * Reads a frame from open file.
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
