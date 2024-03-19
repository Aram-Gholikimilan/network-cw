// IN2011 Computer Networks
// Coursework 2023/2024
//
// This is an example of how the TemporaryNode object can be used.
// It should work with your submission without any changes.
// This should make your testing easier.

// DO NOT EDIT starts
public class CmdLineStore {


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





	public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage error!");
            System.err.println("DSTStoreCmdLine startingNodeName startingNodeAddress key value");
            return;
        } else {
	    // A full node that is running on the network to be a first point of contact
            String startingNodeName = args[0];
	    String startingNodeAddress = args[1];

	    String key = args[2] + '\n';   // All keys have a new line at the end
	    String value = args[3] + '\n'; // All values have a new line at the end

            // Use a TemporaryNode to store the (key, value) pair on the network
            TemporaryNode tn = new TemporaryNode();

	    // Make contact with the 2D#4 network
	    if (tn.start(startingNodeName, startingNodeAddress)) {

		// Store the (key, value) pair
		if (tn.store(key, value)) {
		    System.out.println("Store worked! :-)");
		} else {
		    System.out.println("Store failed! :-(");
		}

	    } else {
		System.err.println("Could not contact network?");
	    }

            return;
        }
    }
}
// DO NOT EDIT ends
