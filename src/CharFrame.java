package src;
public class CharFrame {
    //CONSTANTS
    public static final int TYPE_BITSIZE=8;
    public static final int NUM_BITSIZE=8;
    public static final int CRC_BITSIZE=16;
    public static final String FLAG = "01111110";
    public static final char FLAG_CHAR = '~';

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
     * @param type type of the frame (e.g. 'R' for REJ)
     * @param data content as string of characters
     * @param polynomial polynomial to be used for checksum in bitstring
     */
    public CharFrame( char type, String data, String polynomial) {
        this.type = padLeft(Integer.toBinaryString(type), '0', TYPE_BITSIZE);
        StringBuilder dataBits = new StringBuilder();
        for(int i = 0; i < data.length(); i++)
            dataBits.append(padLeft(Integer.toBinaryString(data.charAt(i)), '0', 8));
        this.data = dataBits.toString();
        this.polynomial = polynomial;

        this.num = null;
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

        // Moved into isValid()
        // if(!CheckSumCalculator.validate(content, polynomial)){
        //     //if checksum is invalid
        //     throw new InvalidFrameException();
        // }
        
        this.type = content.substring(0, TYPE_BITSIZE);
        this.num = content.substring(TYPE_BITSIZE, TYPE_BITSIZE + NUM_BITSIZE);
        this.data = content.substring(TYPE_BITSIZE + NUM_BITSIZE, content.length() - CRC_BITSIZE);
        this.polynomial = polynomial;

    }

    public char getType(){
        return (char) Integer.parseInt(type, 2);
    }

    public void setType(char type){
        this.type = padLeft(Integer.toBinaryString(type), '0', TYPE_BITSIZE);
    }

    public int getNum(){
        return Integer.parseInt(num, 2);
    }

    public void setNum(int num){
        String numstr = Integer.toBinaryString(num);
        if(NUM_BITSIZE < numstr.length()){
            throw new IllegalArgumentException("arg int num is not small enough to be represented over " + NUM_BITSIZE + " bits");
        }
        this.num = padLeft(numstr, '0', NUM_BITSIZE);
    }

    public String getData(){
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < this.data.length(); i+=8) {
            char c = (char) Integer.parseInt(data.substring(i, i + 8), 2);
            str.append(c);
        }

        return str.toString();
    }

    /**
     * @param data as plain text
     */
    public void setData(String data){
        StringBuilder dataBits = new StringBuilder();
        for(int i = 0; i < data.length(); i++)
            dataBits.append(padLeft(Integer.toBinaryString(data.charAt(i)), '0', 8));
        this.data = dataBits.toString();
    }

    public String getPolynomial() {
        return polynomial;
    }

    public void setPolynomial(String polynomial) {
        this.polynomial = polynomial;
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
    public String format() throws InvalidFrameException{
        if(num == null)
            throw new InvalidFrameException();
        return FLAG + BitStuffer.stuff(type + num + data + computeCRC()) + FLAG;
    }

    //TODO: debug
    public boolean isValid() {
        // String crc = CheckSumCalculator.computeCRC(type + num + data, polynomial);
        // return CheckSumCalculator.validate(crc, polynomial);
        return true;
    }

    //debug
    public static void main(String[] args) {
        try {
            // CharFrame cf = new CharFrame("011111100100000100000001001011101101110001111110", Sender.CRC_CCITT);
            CharFrame foo = new CharFrame('I', "test", Sender.CRC_CCITT);
            String data = foo.getData();
            System.out.println(data);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
