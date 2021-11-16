package src;
public class BitStuffer {
    /**
     * @return the bitstring with an added '0' after any 5 consecutive '1''s
     */
    public static String stuff(String bitstring) {
	StringBuilder result = new StringBuilder(bitstring);
	int oneCounter = 0;
	for(int i = 0; i < result.length(); i++){
	    if(result.charAt(i) == '1'){
		oneCounter++;
		if(oneCounter == 5){//insert '0' after 5 '1''s
		    i++;
		    result.insert(i, '0');
		    oneCounter = 0;
		}
	    }
	    else{
		oneCounter = 0;
	    }
	}
	return result.toString();
    }
    /**
     * reverses the stuff(String) function
     */
    public static String destuff(String bitstring) {
	StringBuilder result = new StringBuilder(bitstring);
	int oneCounter = 0;
	for(int i = 0; i < result.length(); i++){
	    if(result.charAt(i) == '1'){
		oneCounter++;
		if(oneCounter == 5){//remove '0' after 5 '1''s
		    result.deleteCharAt(i+1);
		    oneCounter = 0;
		}
	    }
	    else{
		oneCounter = 0;
	    }
	}
	return result.toString();
    }
}
