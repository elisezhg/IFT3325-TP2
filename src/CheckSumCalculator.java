package src;

import java.util.Arrays;

public class CheckSumCalculator {

    // PRIVATE METHODS
    /**
     * Computes the XOR of two binary numbers in string
     * 
     * @param s1 first string
     * @param s2 second string
     * @return the XOR as a string
     */
    private static String xor(String s1, String s2) {

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < Math.min(s1.length(), s2.length()); i++) {
            if (s1.charAt(i) == s2.charAt(i))
                result.append('0');
            else
                result.append('1');
        }

        return result.toString();
    }

    /**
     * Add left-padding to a given string
     * 
     * @param str          string to be padded
     * @param pad          character to pad with
     * @param targetLength target length of the string
     * @return str padded with '0' to the left to reach target length
     */
    private static String padLeft(String str, char pad, int targetLength) {
        StringBuilder result = new StringBuilder(str);
        while (result.length() < targetLength) {
            result.insert(0, pad);
        }
        return result.toString();
    }

    // PUBLIC METHODS
    /**
     * Computes the CRC of a bitstring
     * 
     * @param bitstring  bitstring
     * @param polynomial polynomial of the CRC computation
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
     * Cyclic division rest
     * 
     * @param bitstring  divident
     * @param polynomial divisor
     * @return the rest of the division as a String
     */
    public static String cyclicDivisionRest(String bitstring, String polynomial) {
        StringBuilder rest = new StringBuilder(bitstring);
        for (int i = 0; i <= rest.length() - polynomial.length(); i++) {
            if (rest.charAt(i) == '1') {
                rest.replace(i, i + polynomial.length(),
                        xor(polynomial,
                                rest.substring(i, i + polynomial.length())));
            }
        }
        // remove leading zeros
        while (rest.charAt(0) == '0' && rest.length() > 1) {
            rest.deleteCharAt(0);
        }
        return rest.toString();
    }
}
