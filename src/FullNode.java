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
    private Hashtable<String, String> directory = new Hashtable<>();
    private String startingNodeName;
    private String startingNodeAddress;
    private String startingNodeHost; // Store the starting node host for potential later use
    private int startingNodePort; // Store the starting node port for potential later use
    private byte[] nodeHashID;
    private String nodeTime;
    private BufferedReader reader;
    private Writer writer;
    private boolean isOpen;
    private BufferedReader in;
    private Writer out;
    private Socket clientSocket;
    private String nodeName = "aram.gholikimilan@city.ac.uk:2D#4Impl,1.0,FullNode,1";
    private String nodeAddress = "";
    private String ip;
    private int port;
    int backlog = 5;   //TODO: what number should i give?
    private boolean started = false;


    public boolean listen(String ipAddress, int portNumber) {
        try {
            serverSocket = new ServerSocket(portNumber, backlog);
            System.out.println("FullNode listening on " + ipAddress + ":" + portNumber + ". . .");
            isOpen = true;
            nodeAddress = ipAddress + ":" + portNumber;
            this.port=portNumber;
            this.ip=ipAddress;
            return true;
        } catch (IOException e) {
            System.err.println("Could not listen on " + ipAddress + ":" + portNumber + ". " + e.getMessage());
            return false;
        }
    }
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        this.startingNodeName = startingNodeName;
        this.startingNodeAddress = startingNodeAddress;

        try {


            nodeTime = getCurrentTime();
            NodeInfo newNodeInfo0 = new NodeInfo(startingNodeName, startingNodeAddress, nodeTime);
            nodeHashID = HashID.computeHashID(this.startingNodeName + "\n");
            byte[] sameNodeHashID = HashID.computeHashID(this.startingNodeName + "\n");
            int distance = HashID.calculateDistance(nodeHashID, sameNodeHashID);
            updateNetworkMap(distance, newNodeInfo0);

            String[] address = startingNodeAddress.split(":");
            InetAddress host = InetAddress.getByName(address[0]);
            int port = Integer.parseInt(address[1]);

            clientSocket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new OutputStreamWriter(clientSocket.getOutputStream());

            out.write("START 1 " + nodeName + "\n");    // i added a new line
            out.flush();
            System.out.println("start sent.");

            out.write("NOTIFY? \n" + nodeName + "\n" + nodeAddress + "\n");
            out.flush();
            System.out.println("notify sent.");

            out.write("END " + "NOTIFIED!" +"\n");
            out.flush();
            System.out.println("end sent.");
            clientSocket.close();
            in.close();
            out.close();




            System.out.println("srtdrghjkdfuhjlk");
            String nodeTime2 = getCurrentTime();

//            NodeInfo newNodeInfo1 = new NodeInfo(startingNodeName, startingNodeAddress, nodeTime2);
//            nodeHashID = HashID.computeHashID(nodeName + "\n");
//            byte[] newNodeHashID = HashID.computeHashID(startingNodeName + "\n");
//            int distance2 = HashID.countLeadingMatchingBits(nodeHashID, newNodeHashID);
//            updateNetworkMap(distance2, newNodeInfo1);

            while(isOpen) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("A node is accepted.");
                isConnected = true;
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new OutputStreamWriter(clientSocket.getOutputStream());
//                out.write("START 1 " + nodeName + "\n");    // i added a new line
//                out.flush();

                String[] parts = startingNodeAddress.split(":");
                if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
                startingNodeHost = parts[0];
                startingNodePort = Integer.parseInt(parts[1]);



                String message;
                while (isConnected) {
                    message = in.readLine();
                    if (message != null) {
                        System.out.println(message);
                        handleClient(message);
                        System.out.println("The - " + message + " - is handled!");
                        message = null;
                    }
                }
                clientSocket.close();
            }

        } catch (Exception e) {
            System.out.println("Error during communication with the client: " + e.getMessage());
            e.printStackTrace();
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
                        // TODO: i can have a list of added nodes to my network map and
                        //  if the incoming START is from one of the nodes it should end the connection
                        //  Process START command
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
                        started = false;
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
    // Placeholder for request handling methods
    private void handleStartCommand(String line) throws Exception {
        if(!started) {
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

                    out.write("START " + protocolVersion + " " + nodeName + "\n");
                    out.flush();
                    started = true;

            } else {
                // Handle invalid START command
                System.err.println("Invalid START command received: " + line);
            }
        } else {
            out.write("CONNECTION HAS ALREADY STARTED\n");
            out.flush();
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
            boolean isCurrentNodeClosest = closestNodesAddresses.contains(this.startingNodeName);
            // isCurrentNodeClosest(closestNodesAddresses);

            if (isCurrentNodeClosest) {
                // Store the (key, value) pair if the current node is among the three closest
                directory.put(key, value);
                out.write("SUCCESS\n");
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
        try {
            int keyLinesCount = Integer.parseInt(line.split(" ")[1]); // GET? <number>
            StringBuilder keyBuilder = new StringBuilder();
            for (int i = 0; i < keyLinesCount; i++) {
                keyBuilder.append(in.readLine()).append("\n");
            }
            String key = keyBuilder.toString();
            String value1 =  directory.get(key);

            System.out.println("key: \n" + key);
            System.out.println("value: \n" + value1);
            if (value1 != null) {
            int valueLines = value1.split("\n").length;
            out.write("VALUE " + valueLines + "\n" + value1 + "\n");
            out.flush();
            } else {
                out.write("NOPE\n");
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Error processing GET? request: " + e.getMessage());
        }
    }
    private void handleNotifyRequest() {
        try {
            String nodeName = in.readLine(); // Read node name
            String nodeAddress = in.readLine(); // Read node address

            System.out.println("nodeName: " + nodeName);
            System.out.println("nodeAddress: " + nodeAddress);

            String nodeTime = getCurrentTime();
            byte[] nodeHashID = HashID.computeHashID(nodeName+"\n");
            this.nodeHashID = HashID.computeHashID(this.startingNodeName+"\n");
            int distance = HashID.countLeadingMatchingBits(nodeHashID,this.nodeHashID);
            NodeInfo nodeInfo = new NodeInfo(nodeName,nodeAddress,nodeTime);
            updateNetworkMap(distance,nodeInfo);

            out.write("NOTIFIED\n");
            out.flush();
        } catch (Exception e) {
            System.err.println("Error processing NOTIFY? request: " + e.getMessage());
        }
    }
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
        if (nodeToRemove != null) {
            networkMap2.get(distance).remove(nodeToRemove);
            System.out.println("Removed node with longest duration: " + nodeToRemove.getTime());
        }
    }
    private List<NodeInfo> findClosestNodes2(byte[] targetHashID) throws Exception {
        List<NodeInfo> allNodes = new ArrayList<>();
        // Assuming networkMap2 is a map where keys are distances and values are lists of NodeInfo objects
        for (ArrayList<NodeInfo> nodesAtDistance : networkMap2.values()) {
            allNodes.addAll(nodesAtDistance); // Flattening the list of nodes
        }

        // Calculate distance for each node and sort
        allNodes.sort(Comparator.comparingInt(node -> {
            try {
                return HashID.countLeadingMatchingBits(targetHashID, HashID.computeHashID(node.getNodeName() + "\n"));
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
                int distance = HashID.countLeadingMatchingBits(targetHashIDHex, nodeHashID); // Calculate the distance to the target hashID
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
            out.write("NOTIFY? \n" + fullNodeName + "\n" + fullNodeAddress + "\n");
            out.flush();
            String response = in.readLine();
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

            clientSocket = new Socket(startingNodeHost, startingNodePort);
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new OutputStreamWriter(clientSocket.getOutputStream());

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
            int distance = HashID.countLeadingMatchingBits(nodeHashID,newNodeHashID);
            System.out.println("distance: " + distance);
            fNode.updateNetworkMap(distance,newNodeInfo);

            String newNodeTime1 = getCurrentTime();
            NodeInfo newNodeInfo1 = new NodeInfo("66artin@city.ac.uk:MyCoolImplementation,1.41,test-node-21\n","127.0.0.1:3456",newNodeTime1);
            byte[] newNodeHashID1 = HashID.computeHashID(newNodeInfo1.getNodeName());
            byte[] nodeHashID1 = HashID.computeHashID(startingnodename);
            int distance1 = HashID.countLeadingMatchingBits(nodeHashID1,newNodeHashID1);
            System.out.println("distance2: " + distance1);
            fNode.updateNetworkMap(distance1,newNodeInfo1);

            String newNodeTime2 = getCurrentTime();
            NodeInfo newNodeInfo2 = new NodeInfo("fetul.wejbdwhb@city.ac.uk:MyCoolImplementation,1.41,test-node-22\n","127.0.0.1:3456",newNodeTime2);
            byte[] newNodeHashID2 = HashID.computeHashID(newNodeInfo2.getNodeName());
            byte[] nodeHashID2 = HashID.computeHashID(startingnodename);
            int distance2 = HashID.countLeadingMatchingBits(nodeHashID2,newNodeHashID2);
            System.out.println("distance2: " + distance2);
            fNode.updateNetworkMap(distance2,newNodeInfo2);

            String newNodeTime3 = getCurrentTime();
            NodeInfo newNodeInfo3 = new NodeInfo("eetin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-2\n","127.0.0.1:3456",newNodeTime3);
            byte[] newNodeHashID3 = HashID.computeHashID(newNodeInfo3.getNodeName());
            byte[] nodeHashID3 = HashID.computeHashID(startingnodename);
            int distance3 = HashID.countLeadingMatchingBits(nodeHashID3,newNodeHashID3);
            System.out.println("distance3: " + distance3);
            fNode.updateNetworkMap(distance3,newNodeInfo3);

            fNode.handleIncomingConnections("Aram.Milan@city.ac.uk:Red-Wine", "127.0.0.1:6969");
            System.out.println("DONE!");
        }


    }
}
