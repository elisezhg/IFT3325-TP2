package src;
public class CharFrame {
    private String flag = "01111110";
    private String type;
    private String num;
    private String data;
    private String polynomial;

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
    /**
     * @param flag signals start and end of frame
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
     * @param frame formated frame
     * @param polynomial polynomial to be used for checksum
     * @throws BadTransmissionException if frame has invalid flags or checksum
     */
    public CharFrame(String frame, String polynomial) throws InvalidFrameException{
	//TODO
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
    public void setString(String data){
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
	return flag + BitStuffer.stuff(type + num + data + computeCRC()) + flag;
    }
}
