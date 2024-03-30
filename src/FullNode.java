// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Aram Gholikimilan
// 220036178
// Aram.Gholikimilan@city.ac.uk


import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.spec.ECField;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.ToIntFunction;
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
    private HashMap<Integer, ArrayList<NodeInfo>> networkMap2 = new HashMap<>();

    //private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private Hashtable<String, String> directory = new Hashtable<>();
    private String startingNodeName;
    private String startingNodeAddress;
    private String startingNodeHost; // Store the starting node host for potential later use
    private int startingNodePort; // Store the starting node port for potential later use
    private byte[] nodeHashID;
    private String nodeTime;

    //private Socket clientSocket;
    private BufferedReader reader;
    private Writer writer;
    private boolean isOpen;
    BufferedReader in;
    Writer out;
    Socket clientSocket;
    int backlog = 50;
    public boolean listen(String ipAddress, int portNumber) {
        // Implement this!
        // Return true if the node can accept incoming connections
        // Return false otherwise
        //return true;
        try {
            nodeTime = getCurrentTime();
            NodeInfo newNodeInfo0 = new NodeInfo(startingNodeName, startingNodeAddress, nodeTime);
            nodeHashID = HashID.computeHashID(this.startingNodeName + "\n");
            // byte[] newNodeHashID3 = HashID.computeHashID(newNodeInfo3.getNodeName()+"\n");
            byte[] sameNodeHashID = HashID.computeHashID(this.startingNodeName + "\n");
            int distance = HashID.calculateDistance(nodeHashID, sameNodeHashID);
            updateNetworkMap(distance, newNodeInfo0);

            serverSocket = new ServerSocket(portNumber, backlog);
            System.out.println("FullNode listening on " + ipAddress + ":" + portNumber + ". . .");

            isOpen = true;

            return true;
        } catch (Exception e) {
            System.err.println("Could not listen on " + ipAddress + ":" + portNumber + ". " + e.getMessage());
            return false;
        }
        /*
        try {
            System.out.println("Opening the server socket on port " + portNumber);
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server waiting for a client...");
            isOpen=true;
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected!");
            isConnected = true;

            //Socket clientSocket = serverSocket.accept();
            //System.out.println("Client connected!");
            //handleClient(clientSocket);
            return true;

        } catch (Exception e) {
            System.out.println("Failed to listen on " + ipAddress + ":" + portNumber + ". Error: " + e.getMessage());
            return false;
        }

         */
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
       try {
           this.startingNodeName = startingNodeName;
           this.startingNodeAddress = startingNodeAddress;
           String[] parts = startingNodeAddress.split(":");
           if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
           startingNodeHost = parts[0];
           startingNodePort = Integer.parseInt(parts[1]);
           clientSocket = serverSocket.accept();
           try {
               while (true) {
                   if (clientSocket.isClosed() || clientSocket == null) {
                       clientSocket = serverSocket.accept();
                       isConnected = true;
                       in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                       out = new OutputStreamWriter(clientSocket.getOutputStream());
                       out.write("START 1 " + startingNodeName);
                       out.flush();
                   }
                   if (!clientSocket.isClosed() && in.ready()) {
                       String message;
                       // while (isConnected) {
                       message = in.readLine();
                       if (message != null) {
                           System.out.println(message);
                           handleClient(message);
                           System.out.println("The -- " + message + " -- is handled!");
                       }
                       // }
                   }
               }

           } catch (Exception e) {
               System.out.println("Error during communication with the client: " + e.getMessage());
           }
       }catch (Exception e){
           System.out.println("Error during communication with the client2: " + e.getMessage());
       }
    }

    private enum CommandType {
        START, PUT, GET, NOTIFY, NEAREST, ECHO, END, UNKNOWN
    }

    private CommandType getCommandType(String line) {
        if (line.startsWith("START")) return CommandType.START;
        if (line.startsWith("PUT?")) return CommandType.PUT;
        if (line.startsWith("GET?")) return CommandType.GET;
        if (line.startsWith("NOTIFY?")) return CommandType.NOTIFY;
        if (line.startsWith("NEAREST?")) return CommandType.NEAREST;
        if (line.startsWith("ECHO?")) return CommandType.ECHO;
        if (line.startsWith("END")) return CommandType.END;
        return CommandType.UNKNOWN;
    }

    private void handleClient(String line) {
        try {
            while (line != null) {
                if (!isConnected) {
                    System.out.println("Not connected to any node. Please start connection first");
                    break;
                }

                CommandType commandType = getCommandType(line);

                switch (commandType) {
                    case START:
                        // i can have a list of added nodes to my network map and
                        // if the incoming START is from one of the nodes it should end the connection
                        // Process START command
                        handleStartCommand(line);
                        break;
                    case PUT:
                        // Process PUT? command
                        handlePutRequest(line);
                        break;
                    case GET:
                        // Process GET? command
                        handleGetRequest(line);
                        break;
                    case NOTIFY:
                        // Process NOTIFY? command
                        handleNotifyRequest();
                        break;
                    case NEAREST:
                        // Process NEAREST? command
                        handleNearestRequest(line);
                        break;
                    case ECHO:
                        // ECHO command
                        out.write("OHCE\n");
                        out.flush();
                        break;
                    case END:
                        isConnected = false;
                        clientSocket.close();
                        break; // Exit the loop and close the connection
                    default:
                        System.err.println("Unknown command: " + line);
                        break;
                }
                line = null; // Or read the next line if in a loop
            }
        } catch (Exception e) {
            System.err.println("Error handling client. Error: " + e.getMessage());
        }
    }

    public void end(String reason) {
        try {
            Socket clientSocket = new Socket(startingNodeHost, startingNodePort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // Sending a message to the server at the other end of the socket
            writer.write("END " + reason + "\n");
            writer.flush();

            isConnected = false;
            // Close down the connection
            clientSocket.close();
        } catch (Exception e) {
            System.out.println("Connecting attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Placeholder for request handling methods

    private void handleStartCommand(String line) throws Exception {
        // Split the line by spaces to extract the parts
        String[] parts = line.split(" ");
        if (parts.length >= 3) {
            // Assuming the START command format is: START <number> <string>
            String protocolVersion = parts[1];
            String newNodeName = parts[2]; // This could potentially include more parts if the name contains spaces
            String[] nodeNamePart = newNodeName.split(":");
            String nodeAddress = nodeNamePart[1];


            //TODO: Do i need to add the temporary node to the network map?
            // i think we do not have to add those,
            // so i need an if statement to check if it is FullNode then update the network map,
            // otherwise do not add it.
//            byte[] newNodeHashID = HashID.computeHashID(newNodeName+"\n");
//            int distance = HashID.calculateDistance(nodeHashID, newNodeHashID);
//            String newNodeTime = getCurrentTime();
//            NodeInfo newNodeInfo = new NodeInfo(newNodeName,nodeAddress, newNodeTime);
//            updateNetworkMap(distance, newNodeInfo);

            out.write("START " + protocolVersion + " " + startingNodeName + "\n");
            out.flush();
        } else {
            // Handle invalid START command
            System.err.println("Invalid START command received: " + line);
        }
    }

    public static String getCurrentTime() {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return time.format(formatter);
    }

    private void handlePutRequest(String line) throws IOException {
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
            boolean isCurrentNodeClosest = closestNodesAddresses.contains(this.startingNodeName);      // isCurrentNodeClosest(closestNodesAddresses);

            if (isCurrentNodeClosest) {
                // Store the (key, value) pair if the current node is among the three closest
                directory.put(key, value);
                out.write("SUCCESS\n");
                //System.out.println("valueee: " + value);
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

    private void handleGetRequest(String line) {
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

            String value1 =  directory.get(key); //getValue(key); // Placeholder method

            System.out.println("key: \n" + key);
            System.out.println("value: \n" + value1);
            if (value1 != null) {
//                String[] parts = value1.split("\n");
//                StringBuilder v = new StringBuilder();
//                for (String s : parts) {
//                    v.append(s);
//                }
                //String value = v.toString();
//            if (value1 == null){
//                out.write("NOPE\n");
//                out.flush();
//            }
                System.out.println("value in FullNode : \n"+value1);
            //assert value1 != null;
            int valueLines = value1.split("\n").length;  // parts.length; //value.split("\n", -1).length - 1; // Adjusted to correctly handle the last newline
                out.write("VALUE " + valueLines + "\n" + value1 + "\n");
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

    private void handleNotifyRequest() {
        // Extract and process the NOTIFY? request according to the 2D#4 protocol
        // The requester MAY send a NOTIFY request.  This informs the responder of the address of a full node.
        try {
            String nodeName = in.readLine(); // Read node name
            String nodeAddress = in.readLine(); // Read node address

            String nodeTime = getCurrentTime();
            byte[] nodeHashID = HashID.computeHashID(nodeName+"\n");
            this.nodeHashID = HashID.computeHashID(this.startingNodeName+"\n");
            int distance = HashID.calculateDistance(nodeHashID,this.nodeHashID);

            NodeInfo nodeInfo = new NodeInfo(nodeName,nodeAddress,nodeTime);
            updateNetworkMap(distance,nodeInfo);
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

    /*
    private void handleNearestRequest(String line, BufferedReader in, Writer out) {
        // Extract and process the NEAREST? request according to the 2D#4 protocol
        try {
            //System.out.println(line);
            String hashIDHex = line.split(" ")[1];
            byte[] hashID = hexStringToByteArray(hashIDHex);

            //System.out.println(hashID);

            // Here, find the three closest nodes to the given hashID
            // For simplicity, assume we have a method findClosestNodes(hashID) that returns a list of node info
            List<NodeInfo> closestNodes = findClosestNodesNearest(hashID); // Placeholder method

            out.write("NODES " + closestNodes.size()+'\n');
            out.flush();


            //StringBuilder valueBuilder = new StringBuilder();
            //valueBuilder.append("NODES ").append(closestNodes.size());

            for(NodeInfo n : closestNodes){
                out.write(n.getNodeName());
                out.write(n.getNodeAddress()+'\n');

            }
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


     */

    private void handleNearestRequest(String line) throws Exception {
        String hashIDHex = line.split(" ")[1].trim();
        byte[] hashID = HashID.hexStringToByteArray(hashIDHex);

        List<NodeInfo> closestNodes = findClosestNodes2(hashID);

        // Start building the response
        StringBuilder response = new StringBuilder();
        response.append("NODES ").append(closestNodes.size()).append("\n");

        for (NodeInfo node : closestNodes) {

            // Ensure no extra newlines by trimming the node name and address
            String nodeName = node.getNodeName().trim();
            String nodeAddress = node.getNodeAddress().trim();

            // Append each closest node's information
            response.append(nodeName).append("\n").append(nodeAddress).append("\n");
        }

        // Print the full response for debugging purposes
        //System.out.println("Sending response:\n" + response.toString());

        // Write the response to the output
        out.write(response.toString());
        out.flush();
    }

    private void updateNetworkMap(int distance, NodeInfo nodeInfo) {
        if (!networkMap2.containsKey(distance)) {
            ArrayList<NodeInfo> newNodesList = new ArrayList<>();
            newNodesList.add(nodeInfo);
            networkMap2.put(distance, newNodesList);
        } else if (networkMap2.get(distance).size()<3) {
            if (!networkMap2.get(distance).contains(nodeInfo)) {
                networkMap2.get(distance).add(nodeInfo);
            }
        } else {
            removeLongestRunningNode(distance);
            // add the newest node
            networkMap2.get(distance).add(nodeInfo);
        }
    }

    private void removeLongestRunningNode(int distance) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime longestDuration = LocalTime.MIN;
        NodeInfo nodeToRemove = null; // This will hold the node with the longest time
        for (NodeInfo node : networkMap2.get(distance)) {
            String nodeTime = node.getTime();
            LocalTime time = LocalTime.parse(nodeTime, formatter);
            // Considering each time as a duration from 00:00, find the longest
            if (time.isAfter(longestDuration)) {
                longestDuration = time;
                nodeToRemove = node; // Update the node to remove
            }
        }
        // remove the longest time nodeName
        // the code: . . .
        if (nodeToRemove != null) {
            networkMap2.get(distance).remove(nodeToRemove);
            System.out.println("Removed node with longest duration: " + nodeToRemove.getTime());
        }
    }

    private List<NodeInfo> findClosestNodes2(byte[] targetHashID) throws Exception {
        /*
        // Create a list to store nodes and their distance to the target hashID
        List<NodeInfo> sortedNodes = new ArrayList<>();

        // Iterate through the network map to calculate distances
        for (Map.Entry<Integer, ArrayList<NodeInfo>> entry : networkMap2.entrySet()) {
            for (NodeInfo node : entry.getValue()) {
                int distance = HashID.calculateDistance(targetHashID, HashID.computeHashID(node.getNodeName() + "\n"));
                // Temporarily setting the distance in the NodeInfo object for sorting
                node.setDistance(distance);
                sortedNodes.add(node);
            }
        }

        // Sort the list based on the distance
        Collections.sort(sortedNodes, Comparator.comparingInt(NodeInfo::getDistance));

        // Return the top 3 closest nodes or less if the network is smaller
        return sortedNodes.stream().limit(3).collect(Collectors.toList());

         */

        List<NodeInfo> allNodes = new ArrayList<>();
        // Assuming networkMap2 is a map where keys are distances and values are lists of NodeInfo objects
        for (ArrayList<NodeInfo> nodesAtDistance : networkMap2.values()) {
            allNodes.addAll(nodesAtDistance); // Flattening the list of nodes
        }

        // Calculate distance for each node and sort
        allNodes.sort(Comparator.comparingInt(node -> {
            try {
                return HashID.calculateDistance(targetHashID, HashID.computeHashID(node.getNodeName() + "\n"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));

        // Limit to the closest 3 nodes, or fewer if not available
        return allNodes.stream().limit(3).collect(Collectors.toList());
    }

    private List<String> findClosestNodes(byte[] targetHashIDHex) throws Exception {
        // List to hold nodes and their distances to the target hashID
        HashMap<String, Integer> nodeNameDist = new HashMap<>();

        // Iterate over each node in the networkMap
        for (Map.Entry<Integer, ArrayList<NodeInfo>> entry : networkMap2.entrySet()) {
            ArrayList<NodeInfo> nodeList = entry.getValue();
            // Iterate over each NodeInfo in the list
            for (NodeInfo node : nodeList) {
                // Add the node's name to the list of names
                String nodeName = node.getNodeName(); // Extract the node's name
                byte[] nodeHashID = HashID.computeHashID(nodeName + "\n"); // Compute the hashID for the node name
                int distance = HashID.calculateDistance(targetHashIDHex, nodeHashID); // Calculate the distance to the target hashID
                // Add the node's address and its calculated distance to the list
                nodeNameDist.put(nodeName, distance);
            }

         }

        // Select and return the addresses of the top three closest nodes
        return nodeNameDist.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .limit(3) // Limit to top 3
                .collect(Collectors.toList());
    }

    private List<NodeInfo> findClosestNodesNearest(byte[] targetHashIDHex) throws Exception {
        // Map to hold nodes and their distances to the target hashID
        HashMap<NodeInfo, Integer> nodeDistanceMap = new HashMap<>();

        // Iterate over each node in the networkMap
        for (Map.Entry<Integer, ArrayList<NodeInfo>> entry : networkMap2.entrySet()) {
            ArrayList<NodeInfo> nodeList = entry.getValue();
            // Iterate over each NodeInfo in the list
            for (NodeInfo node : nodeList) {
                String nodeName = node.getNodeName(); // Extract the node's name
                byte[] nodeHashID = HashID.computeHashID(nodeName + "\n"); // Compute the hashID for the node name
                int distance = HashID.calculateDistance(targetHashIDHex, nodeHashID); // Calculate the distance to the target hashID
                // Add the node and its calculated distance to the map
                nodeDistanceMap.put(node, distance);
            }
        }

        // Select and return the NodeInfo objects of the top three closest nodes
        return nodeDistanceMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(3) // Limit to top 3
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Select and return the addresses of the top three closest nodes
        //return distances;
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


    public boolean Notify (String request) {
        try{

            String[] parts = request.split("\n");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
            String fullNodeName = parts[0];
            String fullNodeAddress = parts[1];

            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Writer out = new OutputStreamWriter(clientSocket.getOutputStream());

            // Sending a message to the server at the other end of the socket
            System.out.println("Sending a message to the server");
            out.write("NOTIFY? \n" + fullNodeName + "\n" + fullNodeAddress + "\n");
            out.flush();

            String response = in.readLine();
            System.out.println("The server said : " + response);

            if (response != null && response.startsWith("NOTIFIED"))
            {
                isConnected = true; // Update connection status
                return true;
            }

        } catch (Exception e){
            System.out.println("Connecting attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean start(String startingNodeName, String startingNodeAddress) throws IOException {
        // Implement this!
        // Return true if the 2D#4 network can be contacted
        // Return false if the 2D#4 network can't be contacted
        //return true;
        this.startingNodeName=startingNodeName;
        this.startingNodeAddress=startingNodeAddress;
        Socket clientSocket = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        Writer out = new OutputStreamWriter(clientSocket.getOutputStream());
        try{
            String[] parts = startingNodeAddress.split(":");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
            String IPAddressString = parts[0];
            startingNodeHost = String.valueOf(InetAddress.getByName(IPAddressString));
            startingNodePort = Integer.parseInt(parts[1]);

            //System.out.println("TCPClient connecting to " + startingNodeAddress);
            //System.out.println(startingNodeHost.toString() + "  :  "+startingNodePort);
            clientSocket = new Socket(startingNodeHost, startingNodePort);
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // Sending a message to the server at the other end of the socket
//                System.out.println("Sending a message to the server");
//                System.out.println(startingNodeName);
            writer.write("START 1 " + startingNodeName +"\n");
            writer.flush();

            String response = reader.readLine();
            //System.out.println("The server said : " + response);

            if (response != null && response.startsWith("START"))
            {
                isConnected = true; // Update connection status
                return true;
            }

        } catch (Exception e){
            System.out.println("Connecting attempt failed: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public static void main(String[] args) throws Exception {
        FullNode fNode = new FullNode();
        if (fNode.listen("127.0.0.1", 6969)) {

            String startingnodename ="Aram.Milan@city.ac.uk:Red-Wine\n";

            String newNodeTime = getCurrentTime();
            NodeInfo newNodeInfo = new NodeInfo("2aram.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-0\n","127.0.0.1:3456",newNodeTime);
            byte[] newNodeHashID = HashID.computeHashID(newNodeInfo.getNodeName());
            byte[] nodeHashID = HashID.computeHashID(startingnodename);
            int distance = HashID.calculateDistance(nodeHashID,newNodeHashID);
            System.out.println("distance: " + distance);
            fNode.updateNetworkMap(distance,newNodeInfo);

            String newNodeTime1 = getCurrentTime();
            NodeInfo newNodeInfo1 = new NodeInfo("66artin@city.ac.uk:MyCoolImplementation,1.41,test-node-21\n","127.0.0.1:3456",newNodeTime1);
            byte[] newNodeHashID1 = HashID.computeHashID(newNodeInfo1.getNodeName());
            byte[] nodeHashID1 = HashID.computeHashID(startingnodename);
            int distance1 = HashID.calculateDistance(nodeHashID1,newNodeHashID1);
            System.out.println("distance2: " + distance1);
            fNode.updateNetworkMap(distance1,newNodeInfo1);

            String newNodeTime2 = getCurrentTime();
            NodeInfo newNodeInfo2 = new NodeInfo("fetul.wejbdwhb@city.ac.uk:MyCoolImplementation,1.41,test-node-22\n","127.0.0.1:3456",newNodeTime2);
            byte[] newNodeHashID2 = HashID.computeHashID(newNodeInfo2.getNodeName());
            byte[] nodeHashID2 = HashID.computeHashID(startingnodename);
            int distance2 = HashID.calculateDistance(nodeHashID2,newNodeHashID2);
            System.out.println("distance2: " + distance2);
            fNode.updateNetworkMap(distance2,newNodeInfo2);

            String newNodeTime3 = getCurrentTime();
            NodeInfo newNodeInfo3 = new NodeInfo("eetin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-2\n","127.0.0.1:3456",newNodeTime3);
            byte[] newNodeHashID3 = HashID.computeHashID(newNodeInfo3.getNodeName());
            byte[] nodeHashID3 = HashID.computeHashID(startingnodename);
            int distance3 = HashID.calculateDistance(nodeHashID3,newNodeHashID3);
            System.out.println("distance3: " + distance3);
            fNode.updateNetworkMap(distance3,newNodeInfo3);

            fNode.handleIncomingConnections("Aram.Milan@city.ac.uk:Red-Wine", "127.0.0.1:6969");
            System.out.println("DONE!");
        }


    }
}
