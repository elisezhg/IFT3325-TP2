package src;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FrameFileReader {
    private FileReader reader;
    public FrameFileReader(String filename) throws FileNotFoundException{
	reader = new FileReader(filename);
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
}
