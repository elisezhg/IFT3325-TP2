package src;
import java.util.Arrays;
public class CheckSumCalculator {

    //PRIVATE METHODS
    private static String xor(String s1, String s2){

	StringBuilder result = new StringBuilder();
	for(int i = 0;i < Math.min(s1.length(), s2.length()); i++){
	    if(s1.charAt(i) == s2.charAt(i))
		result.append('0');
	    else
		result.append('1');
	}

	return result.toString();
    }

    private static String cyclicDivisionRest(String bitstring, String polynomial){
	StringBuilder rest = new StringBuilder(bitstring);
	for(int i = 0; i <= rest.length() - polynomial.length(); i++){
	    if(rest.charAt(i) == '1'){
		rest.replace(i, i+polynomial.length(),
				  xor(polynomial,
				      rest.substring(i, i + polynomial.length())));
	    }
	}
	//remove leading zeros
	while (rest.charAt(0) == '0' && rest.length() > 1) {
		rest.deleteCharAt(0);
	}
	return rest.toString();
    }

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
     * @return 16-bit code that allows for error checking
     */
    public static String computeCRC(String bitstring, String polynomial) {
	char[] zeros = new char[polynomial.length() - 1];
	Arrays.fill(zeros, '0');

	StringBuilder dividend = new StringBuilder(bitstring);
	dividend.append(zeros);

	return padLeft(cyclicDivisionRest(dividend.toString(), polynomial), '0', 16);
    }

    /**
     * @return true if the polynomial divides bitstring mod 2
     */
    public static boolean validate(String bitstring, String polynomial) {
	return Integer.parseInt(cyclicDivisionRest(bitstring, polynomial), 2) == 0;
    }

    public static void main(String[] args) {
	String crc = CheckSumCalculator.computeCRC("1000000000000000", Sender.CRC_CCITT);
	System.err.println(crc);
    }

}
