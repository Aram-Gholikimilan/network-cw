// IN2011 Computer Networks
// Coursework 2023/2024
//
// Construct the hashID for a string

import java.lang.StringBuilder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static java.lang.Integer.numberOfLeadingZeros;

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

	public static int calculateDistance(byte[] hashID1, byte[] hashID2){
		int distance = 256; // Start with the maximum distance
		for (int i=0 ; i<hashID1.length && i<hashID2.length; i++){            // XOR the bytes to find differing bits
			int xorResult = hashID1[i] ^ hashID2[i];
			if (xorResult == 0){                 // If XOR result is 0, all bits in this byte match, decrease distance by 8
				distance -= 8;
			} else{                 // If there are differing bits, find the position of the first differing bit in this byte
				for (int bit=7; bit>=0; bit--){
					if ((xorResult & (1 << bit)) != 0){
						distance -= (7-bit);                         // Found the first differing bit, adjust distance and break
						return distance;
					}
				}
			}
		}
		return distance;
	}

	public static int calDistance (byte[] hash1, byte[]hash2){
		byte b1 = 0;
		byte b2 = 0;
		int counter = 0;
		for(int i=0; i< hash1.length;i++){
			if(hash1[i] != hash2[i]){
				b1 = hash1[i];
				b2 = hash2[i];
				break;
			} else {
				counter++;
			}
		}
		byte result = (byte) ((b1 ^ b2));
		int numberDiff = numberOfLeadingZeros(result);
		return 256 - ((counter * 8) -24 + numberDiff);
	}

	public static void main(String[] args){
		try{
			byte[] hashID1 = computeHashID("Hello World!\n");
			byte[] hashID2 = computeHashID("Hello World 2!\n");


			int distance = calculateDistance(hashID1,hashID2);
			System.out.println("Distance " + distance);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
