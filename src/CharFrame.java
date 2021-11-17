package src;
public class CharFrame {
    //CONSTANTS
    private static final int TYPE_BITSIZE=8;
    private static final int NUM_BITSIZE=8;
    private static final int CRC_BITSIZE=16;//if it is determined from polynomial, a constant in CheckSumCalculator.computeCRC has to be changed
    private static final String FLAG = "01111110";

    //FIELDS
    private String type;
    private String num;
    private String data;
    private String polynomial;

    //PRIVATE METHODS
    /**
     * @return str padded with '0' to the left to reach target length
     */
    private static String padLeft(String str, char pad, int targetLength){
	StringBuilder result = new StringBuilder(str);
	while(result.length() < targetLength){
	    result.insert(0, pad);
	}
	return result.toString();
    }
    //PUBLIC METHODS
    /**
     * @param FLAG signals start and end of frame
     * @param type type of the frame (e.g. 'R' for REJ)
     * @param num frame numerotation in the window
     * @param data content as string of characters
     * @param polynomial polynomial to be used for checksum
     */
    public CharFrame( char type, char num, String data, String polynomial) {
	this.type = padLeft(Integer.toBinaryString(type), '0', 8);
	this.num = padLeft(Integer.toBinaryString(num), '0', 8);
	StringBuilder dataBits = new StringBuilder();
	for(int i = 0; i < data.length(); i++)
	    dataBits.append(padLeft(Integer.toBinaryString(data.charAt(i)), '0', 8));
	this.data = dataBits.toString();
	this.polynomial = polynomial;
    }
    /**
     * @param frame formated frame (bitstring)
     * @param polynomial polynomial to be used for checksum
     * @throws BadTransmissionException if frame has invalid flags or checksum
     */
    public CharFrame(String frame, String polynomial) throws InvalidFrameException{
	if(!frame.substring(0, FLAG.length()).equals(FLAG) || !frame.substring(frame.length() - FLAG.length()).equals(FLAG)){
	    //if flag is not found at start and end of frame
	    throw new InvalidFrameException();
	}
	String content = BitStuffer.destuff(frame.substring(FLAG.length(), frame.length() - FLAG.length()));
	if(!CheckSumCalculator.validate(content, polynomial)){
	    //if checksum is invalid
	    throw new InvalidFrameException();
	}
	this.type = content.substring(0, TYPE_BITSIZE);
	this.num = content.substring(TYPE_BITSIZE, TYPE_BITSIZE + NUM_BITSIZE);
	this.data = content.substring(TYPE_BITSIZE + NUM_BITSIZE, content.length() - CRC_BITSIZE);
	this.polynomial = polynomial;

    }

    public char getType(){
	return (char) Integer.parseInt(type, 2);
    }
    public void setType(char type){
	this.type = Integer.toBinaryString(type);
    }
    public char getNum(){
	return (char) Integer.parseInt(num, 2);
    }
    public void setNum(char num){
	this.num = Integer.toBinaryString(num);
    }
    public String getData(){
	return data;
    }
    public void setData(String data){
	this.data = data;
    }
    /**
     * computes checksum for the frame
     * @return checksum
     */
    public String computeCRC() {
	return CheckSumCalculator.computeCRC(type + num + data, polynomial);
    }
    /**
     * computes checksum and adds bitstuffing
     * @return this CharFrame as a string ready to be sent
     */
    public String format() {
	return FLAG + BitStuffer.stuff(type + num + data + computeCRC()) + FLAG;
    }
}
