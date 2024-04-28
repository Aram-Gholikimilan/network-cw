// IN2011 Computer Networks
// Coursework 2023/2024
//
// Construct the hashID for a string

import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import static java.lang.Integer.numberOfLeadingZeros;
import static java.lang.Integer.toBinaryString;

public class HashID {
    public static byte [] computeHashID(String line) throws Exception {
	if (line.endsWith("\n")) {
	    // What this does and how it works is covered in a later lecture
	    MessageDigest md = MessageDigest.getInstance("SHA-256");
	    md.update(line.getBytes(StandardCharsets.UTF_8));
	    return md.digest();

	} else {
	    // 2D#4 computes hashIDs of lines, i.e. strings ending with '\n'
	    throw new Exception("No new line at the end of input to HashID");
	}
	}

	// Helper method to convert byte array to hex string
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

	public static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	public static int countLeadingMatchingBits(byte[] array1, byte[] array2) {
		if (array1 == null || array2 == null) {
			throw new IllegalArgumentException("Input arrays must not be null");
		}

		int minLength = Math.min(array1.length, array2.length);
		int matchingBits = 0;

		for (int i = 0; i < minLength; i++) {
			byte b1 = array1[i];
			byte b2 = array2[i];
			if (b1 == b2) {
				// If the bytes are equal, all 8 bits match.
				matchingBits += 8;
			} else {
				// If not, find the first non-matching bit in the current pair of bytes.
				int matchingBitsInByte = countMatchingBitsInByte(b1, b2);
				matchingBits += matchingBitsInByte;
				// Stop the comparison after finding the first non-matching bit.
				break;
			}
		}

		return 256 - matchingBits;
	}

	private static int countMatchingBitsInByte(byte b1, byte b2) {
		byte xor = (byte) (b1 ^ b2); // XOR to find differing bits
		int count = 0;
		for (int i = 7; i >= 0; i--) {
			if ((xor & (1 << i)) == 0) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}
}
