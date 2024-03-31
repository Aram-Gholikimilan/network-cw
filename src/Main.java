public class Main {
    public static void main(String[] args) {
        try {
            String key = "test/jabberwocky/2";
            String name = "martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-20000";
            byte[] keyHash = HashID.computeHashID(key + "\n");
            byte[] nameHash = HashID.computeHashID(name + "\n");
            int distance = countLeadingMatchingBits(keyHash,nameHash);
            System.out.println("node 0 --> " + distance);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
