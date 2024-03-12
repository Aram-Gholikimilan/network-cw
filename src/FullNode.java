// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Aram Gholikimilan
// 220036178
// Aram.Gholikimilan@city.ac.uk


import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {
    private ServerSocket serverSocket;
    private final ConcurrentHashMap<String, String> networkMap = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ConcurrentHashMap<String, String> keyValueStore = new ConcurrentHashMap<>();
    private String nodeName; // Add this line to store the node's name



    public boolean listen(String ipAddress, int portNumber) {
	// Implement this!
	// Return true if the node can accept incoming connections
	// Return false otherwise
	//return true;

        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Listening on " + ipAddress + ":" + portNumber);
            threadPool.execute(this::acceptConnections);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to listen on " + ipAddress + ":" + portNumber + ". Error: " + e.getMessage());
            return false;
        }

        /*
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server started and listening on port " + portNumber);

            // The server should continuously listen for incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Handle each connection in a separate thread
                new Thread(() -> handleClientConnection(clientSocket)).start();
            }

        } catch (IOException e) {
            System.err.println("Could not listen on port: " + portNumber);
            e.printStackTrace();
            return false;
        }
         */
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
	// Implement this!
	//return;
        try {
            // Split the starting node address into host and port
            String[] parts = startingNodeAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            try (Socket socket = new Socket(host, port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // First, initiate the START message exchange
                out.println("START 1 " + this.nodeName);
                String response = in.readLine(); // Read the starting node's START response

                // Now, notify the starting node of your presence
                out.println("NOTIFY " + this.nodeName + " " + socket.getLocalAddress().getHostAddress() + ":" + this.serverSocket.getLocalPort());
                String notifyResponse = in.readLine(); // Expecting a NOTIFIED response

                // Log or handle the NOTIFY response
                System.out.println("NOTIFY response from starting node: " + notifyResponse);

                // Optionally, you can query the starting node for the nearest nodes or for its network map
                // This can help in populating your node's initial view of the network

            } catch (IOException e) {
                System.err.println("Error handling incoming connections: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number in starting node address.");
        }

    }

    private void acceptConnections() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleClient(clientSocket));
            } catch (IOException e) {
                System.err.println("Error accepting connection. Error: " + e.getMessage());
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("START")) {
                    // Process START command
                    handleStartCommand(line, out);
                    //out.println("START 1 <NodeName>");
                } else if (line.startsWith("PUT?")) {
                    // Process PUT? command
                    handlePutRequest(line, in, out);
                } else if (line.startsWith("GET?")) {
                    // Process GET? command
                    handleGetRequest(line, in, out);
                } else if (line.startsWith("NOTIFY?")) {
                    // Process NOTIFY? command
                    handleNotifyRequest(line, in, out);
                } else if (line.startsWith("NEAREST?")) {
                    // Process NEAREST? command
                    handleNearestRequest(line, in, out);
                } else if (line.startsWith("ECHO?")) {
                    out.println("OHCE");
                } else if (line.startsWith("END")) {
                    break; // Exit the loop and close the connection
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client. Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket. Error: " + e.getMessage());
            }
        }
    }

    // Placeholder for request handling methods

    private void handleStartCommand(String line, PrintWriter out) {
        // Split the line by spaces to extract the parts
        String[] parts = line.split(" ");
        if (parts.length >= 3) {
            // Assuming the START command format is: START <protocol_version> <node_name>
            String protocolVersion = parts[1];
            String nodeName = parts[2]; // This could potentially include more parts if the name contains spaces

            // Handle the extracted protocol version and node name as needed
            // For simplicity, we are just printing them out here
            System.out.println("Received START command from " + nodeName + " with protocol version " + protocolVersion);

            // Respond back with the node's own START message
            // You should replace <NodeName> with this node's actual name
            String thisNodeName = "NO NAME!"; // This should be replaced with your actual node name
            out.println("START " + protocolVersion + " " + thisNodeName);
        } else {
            // Handle invalid START command
            System.err.println("Invalid START command received: " + line);
        }
    }

    private void handlePutRequest(String line, BufferedReader in, PrintWriter out) {
        // Extract and process the PUT? request according to the 2D#4 protocol
        try {
            String[] parts = line.split(" ", 3);
            int keyLinesCount = Integer.parseInt(parts[1]);
            int valueLinesCount = Integer.parseInt(parts[2]);

            StringBuilder keyBuilder = new StringBuilder();
            for (int i = 0; i < keyLinesCount; i++) {
                keyBuilder.append(in.readLine()).append("\n");
            }

            StringBuilder valueBuilder = new StringBuilder();
            for (int i = 0; i < valueLinesCount; i++) {
                valueBuilder.append(in.readLine()).append("\n");
            }

            String key = keyBuilder.toString();
            String value = valueBuilder.toString();

            // Here, implement logic to decide whether to store the key-value pair
            // For simplicity, this example assumes the storage operation is successful
            // You should include your logic for checking hashID distance, etc.

            out.println("SUCCESS"); // Or "FAILED" based on your logic
        } catch (Exception e) {
            System.err.println("Error processing PUT? request: " + e.getMessage());
            out.println("FAILED");
        }
    }

    private void handleGetRequest(String line, BufferedReader in, PrintWriter out) {
        // Extract and process the GET? request according to the 2D#4 protocol
        try {
            int keyLinesCount = Integer.parseInt(line.split(" ")[1]);

            StringBuilder keyBuilder = new StringBuilder();
            for (int i = 0; i < keyLinesCount; i++) {
                keyBuilder.append(in.readLine()).append("\n");
            }

            String key = keyBuilder.toString();

            // Here, implement logic to retrieve the value associated with the key
            // For simplicity, assume we have a method getValue(key) to get the value
            String value = getValue(key); // Placeholder method

            if (value != null) {
                out.println("VALUE " + value.split("\n").length);
                out.print(value);
            } else {
                out.println("NOPE");
            }
        } catch (Exception e) {
            System.err.println("Error processing GET? request: " + e.getMessage());
            // Potentially respond with "NOPE" or a custom error message
        }
    }

    private void handleNotifyRequest(String line, BufferedReader in, PrintWriter out) {
        // Extract and process the NOTIFY? request according to the 2D#4 protocol
        try {
            String nodeName = in.readLine(); // Read node name
            String nodeAddress = in.readLine(); // Read node address

            // Here, update your network map with the new node
            // For simplicity, assume we have a method updateNetworkMap(nodeName, nodeAddress)
            updateNetworkMap(nodeName, nodeAddress); // Placeholder method

            out.println("NOTIFIED");
        } catch (Exception e) {
            System.err.println("Error processing NOTIFY? request: " + e.getMessage());
            // Handle error case, possibly with a specific protocol message
        }
    }

    private void handleNearestRequest(String line, BufferedReader in, PrintWriter out) {
        // Extract and process the NEAREST? request according to the 2D#4 protocol
        try {
            String hashID = line.split(" ")[1];

            // Here, find the three closest nodes to the given hashID
            // For simplicity, assume we have a method findClosestNodes(hashID) that returns a list of node info
            List<String> closestNodes = findClosestNodes(hashID); // Placeholder method

            out.println("NODES " + closestNodes.size());
            for (String nodeInfo : closestNodes) {
                out.println(nodeInfo); // Node name
                //out.println(nodeAddress); // Node address, assume nodeInfo contains this info
            }
        } catch (Exception e) {
            System.err.println("Error processing NEAREST? request: " + e.getMessage());
            // Handle error case
        }
    }

    // Additional helper methods as needed

    private String getValue(String key) {
        // Directly return the value from the store; if key is not present, this returns null
        return keyValueStore.get(key);
    }
    private void updateNetworkMap(String nodeName, String nodeAddress) {
        // Simply put the nodeName and nodeAddress into the map. This updates existing entries or adds new ones.
        networkMap.put(nodeName, nodeAddress);
    }
    private List<String> findClosestNodes(String targetHashIDHex) {
        // This list will hold nodes and their distances to the target hashID
        List<NodeDistance> distances = new ArrayList<>();

        // Calculate the distance between each node's hashID and the target hashID
        for (Map.Entry<String, String> entry : networkMap.entrySet()) {
            String nodeName = entry.getKey();
            // Assuming a method to get the node's hashID in hex format
            String nodeHashIDHex;
            try {
                byte[] nodeHashID = HashID.computeHashID(nodeName + "\n"); // Ensure node names end with a newline character for consistency
                nodeHashIDHex = bytesToHex(nodeHashID); // Convert the byte array to hex string
            } catch (Exception e) {
                e.printStackTrace();
                continue; // Skip this node on error
            }

            int distance = calculateDistance(nodeHashIDHex, targetHashIDHex);
            distances.add(new NodeDistance(entry.getValue(), distance)); // entry.getValue() is assumed to be the node address
        }

        // Sort by distance
        distances.sort(Comparator.comparingInt(NodeDistance::getDistance));

        // Select the top three closest nodes
        return distances.stream().limit(3)
                .map(NodeDistance::getAddress)
                .collect(Collectors.toList());
    }
    private int calculateDistance(String hashID1, String hashID2) {
        // Convert hashIDs from hex to byte arrays
        byte[] hash1 = hexStringToByteArray(hashID1);
        byte[] hash2 = hexStringToByteArray(hashID2);

        // Call your existing HashID.calculateDistance method
        return HashID.calculateDistance(hash1, hash2);
    }
    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    public static byte[] hexStringToByteArray(String hexString) {
        // Normalize the hex string to remove the "0x" prefix if present
        hexString = hexString.startsWith("0x") ? hexString.substring(2) : hexString;

        // Handle the case where the hex string length is odd by prepending a "0"
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }

        // Convert the hex string to a byte array
        byte[] byteArray = new BigInteger(hexString, 16).toByteArray();

        // BigInteger's toByteArray() method returns a byte array containing the two's-complement representation of the BigInteger.
        // The most significant bit is the sign bit (the "0" bit is reserved for positive numbers). If the first byte is 0x00,
        // it means the rest of the array will contain the positive representation of the number. This leading zero byte needs to be removed.
        if (byteArray[0] == 0) {
            byte[] tmp = new byte[byteArray.length - 1];
            System.arraycopy(byteArray, 1, tmp, 0, tmp.length);
            byteArray = tmp;
        }

        return byteArray;
    }


}
