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
	public static int calculateDistance(byte[] hash01, byte[] hash02) {
		String hash1 = bytesToHex(hash01);
		String hash2 = bytesToHex(hash02);
		int traversed = 0;
		String binaryString = "";

		StringBuilder binaryResult = new StringBuilder();
		for (int i = 0; i < Math.min(hash1.length(), hash2.length()); i++) {
			if (hash1.charAt(i) != hash2.charAt(i)) {
				int h1 = Character.digit(hash1.charAt(i), 16);
				int h2 = Character.digit(hash2.charAt(i), 16);
				int xor = h1 ^ h2;
				// Ensure the binary string is 4 digits (one hex character equals 4 binary bits)
				binaryString = String.format("%4s", Integer.toBinaryString(xor)).replace(' ', '0');
				binaryResult.append(binaryString);
				break;
			}else {
				traversed++;
			}

		}

		String finalBinaryString = binaryResult.toString();
		int count = 0;
		for(char c : finalBinaryString.toCharArray()){
			if(c == '0'){
				count++;
			} else {
				break;
			}
		}
		return 256 - (4 * (traversed) + count);
	}
	public static void main(String[] args) throws Exception {
		String hexHash1 = "a890e1aa36481e399939d32680dab2005c299f2bb9c3ba6b151ac0cc821fec7a"; // "0f033be6cea034bd45a0352775a219ef5dc7825ce55d1f7dae9762d80ce64411";
		String hexHash2 = "b97835cb52c81981355dcbc78c1f6167dbcce122004ebb202bdda90cb86ad0e6"; // "0f0139b167bb7b4a416b8f6a7e0daa7e24a08172b9892171e5fdc615bb7f999b";

		// Convert hex strings to byte arrays
		byte[] hash1 = hexStringToByteArray(hexHash1);
		byte[] hash2 = hexStringToByteArray(hexHash2);

		// Calculate and print the distance between the two hash IDs
		int distance = calculateDistance(hash1, hash2);
		System.out.println("Distance: " + distance);

		/*
		// Two example strings, ensuring they end with a newline as required
		String text1 = "Hello World!\n";
		String text2 = "Hello World!?\n"; // Slightly different to ensure some difference in hash

		// Compute hash IDs for both strings
		byte[] hash1 = computeHashID("martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-2\n");
		byte[] hash2 = computeHashID("Welcome\n");

		// Convert hash bytes to hex format for displaying
		String hexHash1 = bytesToHex(hash1);
		String hexHash2 = bytesToHex(hash2);

		// Print out the hash IDs in hex format
		System.out.println("HashID 1: " + hexHash1);
		System.out.println("HashID 2: " + hexHash2);

		// Calculate and print the distance between the two hash IDs
		int distance = calDistance2(hash1, hash2);
		System.out.println("Distance: " + distance);

		 */
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
