package src;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FrameFileReader {
    public static int FRAME_ENCODING_BITS = 3;

    private int counter = 0;
    // private FileReader reader;
    private BufferedReader reader;

    public FrameFileReader(String filename) throws FileNotFoundException{
        // reader = new FileReader(filename);
        reader = new BufferedReader(new FileReader(filename));
    }

    public void closeFile() throws IOException{
        reader.close();
    }

    /**
     * reads a frame from open file.
     * expected format is :
     *
     * {FLAG}{TYPE}{NUM}{DATA}{FLAG}\n
     * {FLAG}{TYPE}{NUM}{DATA}{FLAG}\n
     * etc.
     *
     * @return read frame
     */
    public CharFrame getNextFrame(String polynomial) throws IOException, InvalidFrameFileException{
        if(reader.read() != CharFrame.FLAG_CHAR)
            throw new InvalidFrameFileException();

        int type = reader.read();
        int num = reader.read();

        StringBuilder data = new StringBuilder();
        for (int d = reader.read(); d != CharFrame.FLAG_CHAR; d = reader.read()) {
            if(d < 0)//unexpected EOF
                throw new InvalidFrameFileException();
            data.append((char) d);
        }

        if(reader.read() != '\n')
            throw new InvalidFrameFileException();

        return new CharFrame((char)type,(char) num, data.toString(), polynomial);
    }

    // Reads the next line from the open file and constructs a new frame
    public CharFrame getNextFrame_() throws IOException{

        String line = reader.readLine();

        // EOF
        if (line == null) {
            reader.close();
            return null;
        }

        CharFrame frame = new CharFrame('I', (char) this.counter, line, null);
        this.counter = (this.counter + 1) % (int) Math.pow(2, FRAME_ENCODING_BITS);

        return frame;
    }
}
