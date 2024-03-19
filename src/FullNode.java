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
import java.security.spec.ECField;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {
    private boolean isConnected;
    public ServerSocket serverSocket;
    private final HashMap<String, String> networkMap = new HashMap<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private Hashtable<String, String> directory = new Hashtable<>();
    private ConcurrentHashMap<String, String> keyValueStore = new ConcurrentHashMap<>();
    private String startingNodeName;
    private String startingNodeAddress;
    private String startingNodeHost; // Store the starting node host for potential later use
    private int startingNodePort; // Store the starting node port for potential later use

    public boolean listen(String ipAddress, int portNumber) {
	// Implement this!
	// Return true if the node can accept incoming connections
	// Return false otherwise
	//return true;
        try{
            System.out.println("Opening the server socket on port " + portNumber);
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server waiting for a client...");

            //Socket clientSocket = serverSocket.accept();
            //System.out.println("Client connected!");
            //handleClient(clientSocket);
            return true;

        } catch (Exception e){
            System.out.println("Failed to listen on " + ipAddress + ":" + portNumber + ". Error: " + e.getMessage());
            return false;
        }
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
	// Implement this!
	//return;

        this.startingNodeName=startingNodeName;
        this.startingNodeAddress=startingNodeAddress;
        networkMap.put(startingNodeName,startingNodeAddress);
        try{
            String[] parts = startingNodeAddress.split(":");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
            startingNodeHost = parts[0];
            startingNodePort = Integer.parseInt(parts[1]);

            //serverSocket = new ServerSocket(startingNodePort);
            //Socket clientSocket = serverSocket.accept();
            //System.out.println("Client connected!");
            // handleClient(clientSocket);
            //System.out.println("Waiting for a client...!");
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected!");

            // Handle each connection in a separate thread
            isConnected = true;


            while (isConnected) {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Writer out = new OutputStreamWriter(clientSocket.getOutputStream());

                String message = in.readLine();
                System.out.println(message);
                handleClient(message, in, out);
                //new Thread(() -> handleClient(message,in,out)).start();
                System.out.println("The client is handled: " + message);
                //clientSocket.close();
            }
        } catch (Exception e){
            System.out.println("eeerroorr: "+e.getMessage());
        }
    }

    private void handleClient(String line,BufferedReader in, Writer out) {

        try{
            while (line != null) {
                if (!isConnected){
                    System.out.println("Not connected to any node. Please start connection first");
                    break;
                }
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
                    //  This message allows the requester to tell whether the connection is
                    //  still active and the responder is still working correctly.
                    out.write("OHCE\n");
                    out.flush();
                } else if (line.startsWith("END")) {
                    isConnected = false;
                    //serverSocket.close();
                    break; // Exit the loop and close the connection
                }
                line=null;
            }

        } catch (IOException e) {
            System.err.println("Error handling client. Error: " + e.getMessage());
        }
    }

    public void end (String reason){
        try{
            System.out.println("TCPClient connecting to " + startingNodeAddress);
            System.out.println(startingNodeHost.toString() + "  :  "+startingNodePort);
            Socket clientSocket = new Socket(startingNodeHost, startingNodePort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // Sending a message to the server at the other end of the socket
            System.out.println("Sending a message to the server");
            writer.write("END " + reason +"\n");
            writer.flush();

            isConnected = false;
            // Close down the connection
            clientSocket.close();
        } catch (Exception e){
            System.out.println("Connecting attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Placeholder for request handling methods

    private void handleStartCommand(String line, Writer out) throws IOException {
        // Split the line by spaces to extract the parts
        String[] parts = line.split(" ");
        if (parts.length >= 3) {
            // Assuming the START command format is: START <number> <string>
            String protocolVersion = parts[1];
            String nodeName = parts[2]; // This could potentially include more parts if the name contains spaces
            out.write("START " + protocolVersion + " " + startingNodeName +"\n");
            out.flush();

        } else {
            // Handle invalid START command
            System.err.println("Invalid START command received: " + line);
        }
    }

   /* private void handlePutRequest(String line, BufferedReader in, Writer out) throws IOException {

        // Extract and process the PUT? request according to the 2D#4 protocol
        try {
            //   PUT? 1 2
            //   Welcome
            //   Hello
            //   World!
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

            System.out.println("key: \n" + key);
            System.out.println("value: \n" + value);

            //When the responder gets a PUT request it must compute the hashID
            //   for the value to be stored.  Then it must check the network
            //   directory for the three closest nodes to the key's hashID.  If the
            //   responder is one of the three nodes that are closest then
            //   it MUST store the (key, value) pair and MUST respond with a single
            //   line:
            //   SUCCESS
            byte[] keyHashID = HashID.computeHashID(key);
            List<String> closestNodes  = findClosestNodes(keyHashID);
            byte[] currentNodeHashID = HashID.computeHashID(this.startingNodeName);
//            if(threeClosestNodes.contains()){
//                out.write("FAILED\n");
//                out.flush();
//            }

            boolean isCurrentNodeClosest = closestNodes.stream()
                    .anyMatch(nodeDistance -> Arrays.equals(nodeDistance.getNodeHashID(), currentNodeHashID));

            if (isCurrentNodeClosest) {
                // Current node is one of the three closest nodes
                directory.put(key, value); // Store the key-value pair
                out.write("SUCCESS\n");
            } else {
                // There are three other nodes closer to the key's hashID
                out.write("FAILED\n");
            }
        } catch (Exception e) {
            System.err.println("Error processing PUT? request: " + e.getMessage());
            out.write("FAILED\n");
            out.flush();
        }
    }*/

    private void handlePutRequest(String line, BufferedReader in, Writer out) throws IOException {
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

            // Compute the hashID for the key
            byte[] keyHashID = HashID.computeHashID(key);

            // Determine the three closest nodes to the key's hashID
            List<String> closestNodesAddresses = findClosestNodes(keyHashID);

            // Check if the current node is among the three closest
            boolean isCurrentNodeClosest = isCurrentNodeClosest(closestNodesAddresses);

            if (isCurrentNodeClosest) {
                // Store the (key, value) pair if the current node is among the three closest
                directory.put(key, value);
                out.write("SUCCESS\n");
                System.out.println("valueee: "+ value);
            } else {
                out.write("FAILED\n");
            }
        } catch (Exception e) {
            System.err.println("Error processing PUT? request: " + e.getMessage());
            out.write("FAILED\n");
        } finally {
            out.flush();
        }
    }

    private boolean isCurrentNodeClosest(List<String> closestNodesAddresses) throws Exception {
        // Compute the current node's hashID from its name
        //byte[] currentNodeHashID = HashID.computeHashID(this.startingNodeName + "\n");

        // You need to implement a way to determine if the current node's address is among the closest nodes' addresses
        // This is a conceptual implementation; adjust according to your network map and addressing scheme
        String currentNodeAddress = this.startingNodeAddress;
        return closestNodesAddresses.contains(currentNodeAddress);
    }

    //TODO: Extract and process the GET? request according to the 2D#4 protocol and the output
    private void handleGetRequest(String line, BufferedReader in, Writer out) {
        //Extract and process the GET? request according to the 2D#4 protocol
        try {
            int keyLinesCount = Integer.parseInt(line.split(" ")[1]); // GET? <number>

            StringBuilder keyBuilder = new StringBuilder();
            for (int i = 0; i < keyLinesCount; i++) {
                keyBuilder.append(in.readLine()).append("\n");
            }


            String key = keyBuilder.toString();

            //The responder MUST compute the hashID of the key.
            // Here, implement logic to retrieve the value associated with the key
            // For simplicity, assume we have a method getValue(key) to get the value
            String value1 = getValue(key); // Placeholder method

            System.out.println("key: \n" + key);
            System.out.println("value: \n" + value1);
            if (value1 != null) {
                String[] parts = value1.split("\n");
                StringBuilder v = new StringBuilder();
                for (String s : parts){
                    v.append(s);
                }
                String value = v.toString();
                int valueLines = parts.length; //value.split("\n", -1).length - 1; // Adjusted to correctly handle the last newline
                out.write("VALUE " + valueLines+"\n");
                //out.flush();
                //out.write(value +"\n");
                out.flush();
            } else {
                out.write("NOPE\n");
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Error processing GET? request: " + e.getMessage());
            // Potentially respond with "NOPE" or a custom error message
        }
    }

    //TODO: Extract and process the NOTIFY? request according to the 2D#4 protocol
    private void handleNotifyRequest(String line, BufferedReader in, Writer out) {
        // Extract and process the NOTIFY? request according to the 2D#4 protocol
        // The requester MAY send a NOTIFY request.  This informs the responder of the address of a full node.
        try {
            String nodeName = in.readLine(); // Read node name
            String nodeAddress = in.readLine(); // Read node address

            // Here, update your network map with the new node
            // For simplicity, assume we have a method updateNetworkMap(nodeName, nodeAddress)
            //updateNetworkMap(nodeName, nodeAddress); // Placeholder method

            out.write("NOTIFIED\n");
            out.flush();
        } catch (Exception e) {
            System.err.println("Error processing NOTIFY? request: " + e.getMessage());
            // Handle error case, possibly with a specific protocol message
        }
    }

    // TODO: Extract and process the NEAREST? request according to the 2D#4 protocol
    private void handleNearestRequest(String line, BufferedReader in, Writer out) {
        // Extract and process the NEAREST? request according to the 2D#4 protocol
        try {
            //System.out.println(line);
            String hashIDHex = line.split(" ")[1];
            byte[] hashID = hexStringToByteArray(hashIDHex);

            //System.out.println(hashID);

            // Here, find the three closest nodes to the given hashID
            // For simplicity, assume we have a method findClosestNodes(hashID) that returns a list of node info
            List<String> closestNodes = findClosestNodes(hashID); // Placeholder method

            out.write("NODES " + closestNodes.size() +"\n");
            out.flush();
            //for (String nodeInfo : closestNodes) {
              //  out.write(nodeInfo); // Node name
                //out.flush();
                //out.println(nodeAddress); // Node address, assume nodeInfo contains this info
            //}
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
    private List<String> findClosestNodes(byte[] targetHashIDHex) throws Exception {
        // List to hold nodes and their distances to the target hashID
        List<NodeDistance> distances = new ArrayList<>();

        // Iterate over each node in the networkMap
        for (Map.Entry<String, String> entry : networkMap.entrySet()) {
            String nodeName = entry.getKey(); // Extract the node's name
            byte[] nodeHashID = HashID.computeHashID(nodeName + "\n"); // Compute the hashID for the node name
            int distance = HashID.calDistance2(targetHashIDHex, nodeHashID); // Calculate the distance to the target hashID

            // Add the node's address and its calculated distance to the list
            distances.add(new NodeDistance(entry.getValue(), distance));
        }

        // Sort the list of nodes by their distance to the target hashID in ascending order
        distances.sort(Comparator.comparingInt(NodeDistance::getDistance));

        // Select and return the addresses of the top three closest nodes
        return distances.stream()
                .limit(3)
                .map(NodeDistance::getValue) // Extract the node's address
                .collect(Collectors.toList());
    }


    /*private List<String> findClosestNodes(byte[] targetHashIDHex) throws Exception {
        // This list will hold nodes and their distances to the target hashID
        List<NodeDistance> distances = new ArrayList<>();

        // Iterate through the networkMap to calculate distances based on node names
        for (Map.Entry<String, String> entry : networkMap.entrySet()) {
            String nodeName = entry.getKey(); // Node name is the key in 'networkMap'
            byte[] nodeHashID = HashID.computeHashID(nodeName + "\n"); // Compute hashID for the node name
            int distance = HashID.calDistance(targetHashIDHex, nodeHashID); // Calculate distance
            distances.add(new NodeDistance(entry.getValue(), distance)); // Add node address and distance
        }

        // Sort by distance
        distances.sort(Comparator.comparingInt(NodeDistance::getDistance));

        // Select the top three closest nodes
        return distances.stream()
                .limit(3)
                .map(NodeDistance::getValue) // 'getValue' returns the node's address
                .collect(Collectors.toList());
/*
//        // This list will hold nodes and their distances to the target hashID
//        List<NodeDistance> distances = new ArrayList<>();
//
//        // Calculate the distance between each node's hashID and the target hashID
//
//        // Getting the enumeration of keys
//        Enumeration<String> keys = directory.keys();
//
//        // Iterating through the enumeration of keys
//        while (keys.hasMoreElements()) {
//            // Accessing each key
//            String key = keys.nextElement();
//            byte[] nodeHashID = HashID.computeHashID(key + "\n");
//            int distance = HashID.calDistance(targetHashIDHex,nodeHashID);
//            distances.add(new NodeDistance(directory.get(key), distance));
//        }
//
////         When the responder gets a PUT request it must compute the hashID
////   for the value to be stored.  Then it must check the network
////   directory for the three closest nodes to the key's hashID.  If the
////   responder is one of the three nodes that are closest then
////   it MUST store the (key, value) pair and MUST respond with a single
////   line:
//
//
////        for (Map.Entry<String,String> entry : directory.keySet()) {
////            String nodeName = entry.getKey();
////            byte[] nodeHashID = HashID.computeHashID(nodeName + "\n");
////            int distance = HashID.calDistance(targetHashIDHex,nodeHashID);
////            distances.add(new NodeDistance(entry.getValue(), distance)); // entry.getValue() is assumed to be the node address
////       }
//
//        // Sort by distance
//        distances.sort(Comparator.comparingInt(NodeDistance::getDistance));
//        System.out.println(distances);
//        // Select the top three closest nodes
//        return distances.stream().limit(3)
//                .map(NodeDistance::getValue)
//
            .collect(Collectors.toList());

    }
     */
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


    public static void main(String[] args) throws IOException {
        FullNode fNode = new FullNode();
        if (fNode.listen("127.0.0.1", 3456)) {
            fNode.networkMap.put("aram.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-20","127.0.0.1:3457");
            fNode.networkMap.put("martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-21","127.0.0.1:3456");
            fNode.networkMap.put("martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-22","127.0.0.1:3457");
            fNode.networkMap.put("metin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-2","127.0.0.1:3456");
            fNode.handleIncomingConnections("martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-2", "127.0.0.1:3456");
            System.out.println("DONE!");
        }


    }
}
