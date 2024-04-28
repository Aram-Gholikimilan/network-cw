// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Aram Gholikimilan
// 220036178
// Aram.Gholikimilan@city.ac.uk


import java.io.*;
import java.net.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends

//   Temporary
//   nodes are limited members of the network.  They do not store values
//   or respond to requests.
public class TemporaryNode implements TemporaryNodeInterface {
    private Socket clientSocket;
    private BufferedReader reader;
    private Writer writer;
    private String startingNodeName;
    private String startingNodeAddress;
    private InetAddress startingNodeHost; // Store the starting node host for potential later use
    private int startingNodePort; // Store the starting node port for potential later use
    private boolean isConnected = false; // Keep track of the connection state
    String name = "aram@city.ac.uk:12345";
    String host = "127.0.0.1";
    String port = "6969";
    public boolean start(String startingNodeName, String startingNodeAddress) {
        this.startingNodeName=startingNodeName;
        this.startingNodeAddress=startingNodeAddress;
        try{
            String[] parts = startingNodeAddress.split(":");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
            String IPAddressString = parts[0];
            startingNodeHost = InetAddress.getByName(IPAddressString);
            startingNodePort = Integer.parseInt(parts[1]);
            clientSocket = new Socket(startingNodeHost, startingNodePort);
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new OutputStreamWriter(clientSocket.getOutputStream());
            writer.write("START 1 " + name +"\n");
            writer.flush();
            String response = reader.readLine();
            System.out.println("For START, The FullNode said : " + response);
            if (response != null && response.startsWith("START"))
            {
                isConnected = true; // Update connection status
                return true;
            }
        } catch (Exception e){
            System.out.println("Connecting attempt failed: " + e.getMessage());
            e.printStackTrace();
        }

        end("INVALID RESPONSE");
        return false;
    }

    public boolean store(String key, String value) {
        int min=257;
        int distance=257;
        String minNodeName=this.startingNodeName;
        String minNodeAddress=this.startingNodeAddress;

        ArrayList<String> visitedNodes = new ArrayList<>();
        visitedNodes.add(startingNodeName);
        try {
            while(true) {
                int keyLines = key.split("\n").length;
                int valueLines = value.split("\n").length;

                System.out.println("Sending a message to the server");
                writer.write("PUT? " + keyLines + " " + valueLines + "\n" + key + value); //  + "\n" + key + "\n" + value
                writer.flush();

                String response = reader.readLine();
                System.out.println("put response: " + response);

                if (response != null && response.startsWith("SUCCESS")) {
                    clientSocket.close();
                    return true;
                } else if (response.startsWith("FAILED")) {
        //    while(true){
                    byte[] keyHash = HashID.computeHashID(key);
                    String hexKeyHash = HashID.bytesToHex(keyHash);

                    String nearestNodesInfo = nearest(hexKeyHash);

                    if (nearestNodesInfo == null || nearestNodesInfo.isEmpty()) {
                        System.err.println("Failed to retrieve nearest nodes or none are available.");
                        end("COMPLETE");
                        return false;
                    }

                    // TreeMap to store distances and corresponding node names
                    TreeMap<Integer, List<String[]>> sortedNodes = convertStringNodesToMap(nearestNodesInfo, key);

                    // Now sortedNodes contains all node names sorted by the distance in ascending order
                    List<String> sortedNamesByDistance = new ArrayList<>();
                    List<String[]> nodeDetails = new ArrayList<>();

                    for (Map.Entry<Integer, List<String[]>> entry : sortedNodes.entrySet()) {
                        nodeDetails = entry.getValue(); // List of node details arrays

                    }

                    String minNodeName2=null;
                    String minNodeAddress2=null;

                // Check if the TreeMap is not empty to prevent accessing non-existing entries
                if (!sortedNodes.isEmpty()) {
                    // Retrieve the first (minimum distance) entry from the TreeMap
                    Map.Entry<Integer, List<String[]>> firstEntry = sortedNodes.firstEntry();

                    // Check if there is at least one node in the list of this entry
                    if (!firstEntry.getValue().isEmpty()) {
                        // Get the first node's details from the list
                        String[] details = firstEntry.getValue().get(0);
                        minNodeName2 = details[0];  // Node name
                        minNodeAddress2 = details[1];  // Node address
                    }
                }

                    for (String[] details : nodeDetails) {
                        String nodeName = details[0]; // Node name
                        String nodeAddress = details[1]; // Node address


                    /*
                    // Split the nearestNodesInfo to get individual node details
                    String[] nodeDetails = nearestNodesInfo.split("\n");
                    int numNodes = Integer.parseInt(nodeDetails[0].split(" ")[1]);
                    // Skip the first line which is "NODES X"
                    for (int i = 1; i < numNodes * 2; i += 2) {
                        String nodeName = nodeDetails[i];
                        String nodeAddress = nodeDetails[i + 1];

                        byte[] nodeHashID = HashID.computeHashID(nodeName + "\n");
                        byte[] keyHashId = HashID.computeHashID(key + "\n");
                        //int distance = HashID.calculateDistance(nodeHashID, keyHashId);
                        distance = HashID.countLeadingMatchingBits(nodeHashID, keyHashId);
                        if (distance < min) {
                            min = distance;
                            minNodeName = nodeName;
                            minNodeAddress = nodeAddress;
                        }
                    }
                    min = 257;
                    distance = 257;

                     */


                        end("CANNOT-STORE");
                        clientSocket.close();
                        reader.close();
                        writer.close();

                        //Once smallest node is found,
                        // check if we have already visited it.
                        if (visitedNodes.contains(nodeName)) {
                            return false;
                        }

                        String[] address = nodeAddress.split(":");
                        int port = Integer.parseInt(address[1]);
                        InetAddress host = InetAddress.getByName(address[0]);
                        clientSocket = new Socket(host, port);

                        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        writer = new OutputStreamWriter(clientSocket.getOutputStream());


                        writer.write("START 1 " + name + "\n");
                        writer.flush();
                        String r = reader.readLine();
                        System.out.println("here --> :" + r);
                        visitedNodes.add(nodeName);

                        writer.write("PUT? " + keyLines + " " + valueLines + "\n" + key + value); //  + "\n" + key + "\n" + value
                        writer.flush();

                        String response2 = reader.readLine();
                        System.out.println("put response: " + response2);
                        if (response2 != null && response2.startsWith("SUCCESS")) {
                            clientSocket.close();
                            return true;
                        }
                    }
                end("CANNOT-STORE");
                clientSocket.close();
                reader.close();
                writer.close();

                String[] address = minNodeAddress2.split(":");
                int port = Integer.parseInt(address[1]);
                InetAddress host = InetAddress.getByName(address[0]);
                clientSocket = new Socket(host, port);

                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new OutputStreamWriter(clientSocket.getOutputStream());


                writer.write("START 1 " + name + "\n");
                writer.flush();
                String r = reader.readLine();
                System.out.println("here --> :" + r);
                visitedNodes.add(minNodeName2);
                }
            }
        } catch (Exception e){
            System.out.println("Error during PUT? request handling (Store operation): "+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public String get(String key) {
        int min=257;
        int distance=257;
        String minNodeName=this.startingNodeName;
        String minNodeAddress=this.startingNodeAddress;

        ArrayList<String> visitedNodes = new ArrayList<>();
        visitedNodes.add(startingNodeName);
        try {
            while(true) {

                int keyLines = key.split("\n").length;

                writer.write("GET? " + keyLines + "\n" + key);
                writer.flush();

                String response = reader.readLine();
                System.out.println("get response: " + response);

                if (response != null && response.startsWith("VALUE")) {
                    clientSocket.close();

                    String[] parts = response.split(" ", 2);
                    int valueLinesCount = Integer.parseInt(parts[1]);
                    StringBuilder valueBuilder = new StringBuilder();
                    for (int i = 0; i < valueLinesCount; i++) {
                        valueBuilder.append(reader.readLine()).append("\n");
                    }

                    String value = valueBuilder.toString();
                    //System.out.println("valueeee:\n"+value);
                    return value;
                } else if (response.startsWith("NOPE")) {
                    // Get the hash ID for the key to find nearest nodes
                    byte[] keyHash = HashID.computeHashID(key);
                    String hexKeyHash = HashID.bytesToHex(keyHash);

                    // Call nearest to find nearest nodes
                    String nearestNodesInfo = nearest(hexKeyHash);


                    if (nearestNodesInfo == null || nearestNodesInfo.isEmpty()) {
                        System.err.println("Failed to retrieve nearest nodes or none are available.");
                        end("COMPLETE");
                        return null;
                    }

                    // TreeMap to store distances and corresponding node names
                    TreeMap<Integer, List<String[]>> sortedNodes = convertStringNodesToMap(nearestNodesInfo, key);

                    // Now sortedNodes contains all node names sorted by the distance in ascending order
                    List<String> sortedNamesByDistance = new ArrayList<>();
                    List<String[]> nodeDetails = new ArrayList<>();

                    for (Map.Entry<Integer, List<String[]>> entry : sortedNodes.entrySet()) {
                        nodeDetails = entry.getValue(); // List of node details arrays

                    }

                    String minNodeName2 = null;
                    String minNodeAddress2 = null;

                    // Check if the TreeMap is not empty to prevent accessing non-existing entries
                    if (!sortedNodes.isEmpty()) {
                        // Retrieve the first (minimum distance) entry from the TreeMap
                        Map.Entry<Integer, List<String[]>> firstEntry = sortedNodes.firstEntry();

                        // Check if there is at least one node in the list of this entry
                        if (!firstEntry.getValue().isEmpty()) {
                            // Get the first node's details from the list
                            String[] details = firstEntry.getValue().get(0);
                            minNodeName2 = details[0];  // Node name
                            minNodeAddress2 = details[1];  // Node address
                        }
                    }

                    for (String[] details : nodeDetails) {
                        String nodeName = details[0]; // Node name
                        String nodeAddress = details[1]; // Node address

                    /*
                    // Split the nearestNodesInfo to get individual node details
                    String[] nodeDetails = nearestNodesInfo.split("\n");
                    System.out.println("nearest nodes: \n" + nearestNodesInfo);
                    int numNodes = Integer.parseInt(nodeDetails[0].split(" ")[1]);
                    // Skip the first line which is "NODES X"
                    for (int i = 1; i < numNodes * 2; i += 2) {
                        String nodeName = nodeDetails[i];
                        String nodeAddress = nodeDetails[i + 1];

                        byte[] nodeHashID = HashID.computeHashID(nodeName + "\n");
                        byte[] keyHashId = HashID.computeHashID(key + "\n");
                        //distance = HashID.calculateDistance(nodeHashID, keyHashId);
                        distance = HashID.countLeadingMatchingBits(nodeHashID, keyHashId);
                        System.out.println(nodeName+" d: "+distance);
                        if (distance < min) {
                            min = distance;
                            minNodeName = nodeName;
                            minNodeAddress = nodeAddress;
                        }

                    }
                    min = 257;
                    distance = 257;

                    System.out.println("min node: "+ minNodeName);


                     */
                        end("CANNOT-GET");
                        clientSocket.close();
                        reader.close();
                        writer.close();

                        if (visitedNodes.contains(minNodeName2)) {
                            System.out.println("* dont want to loop *");
                            return null;
                        }

                        String[] address = minNodeAddress2.split(":");
                        int port = Integer.parseInt(address[1]);
                        InetAddress host = InetAddress.getByName(address[0]);
                        clientSocket = new Socket(host, port);

                        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        writer = new OutputStreamWriter(clientSocket.getOutputStream());


                        writer.write("START 1 " + name + "\n");
                        writer.flush();
                        String r = reader.readLine();
                        visitedNodes.add(minNodeName2);

                        writer.write("GET? " + keyLines + "\n" + key);
                        writer.flush();

                        String response2 = reader.readLine();
                        System.out.println("get response: " + response2);

                        if (response2 != null && response2.startsWith("VALUE")) {
                            clientSocket.close();

                            String[] parts = response2.split(" ", 2);
                            int valueLinesCount2 = Integer.parseInt(parts[1]);
                            StringBuilder valueBuilder = new StringBuilder();
                            for (int i = 0; i < valueLinesCount2; i++) {
                                valueBuilder.append(reader.readLine()).append("\n");
                            }

                            String value2 = valueBuilder.toString();
                            //System.out.println("valueeee:\n"+value);
                            return value2;
                        }
                    }

                }
            }
        } catch (Exception e){
            System.out.println("Error during GET? request handling: "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    public void end (String reason){
        try{
            writer.write("END " + reason +"\n");
            writer.flush();
            isConnected = false;
            // Close down the connection
            clientSocket.close();
            System.out.println("connection ended.");
        } catch (Exception e){
            System.out.println("Connecting attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public boolean echo (){
        if (!isConnected){
            System.out.println("Not connected to any node. Please start connection first");
            return false;
        }
        try{
            writer.write("ECHO?" +"\n");
            writer.flush();

            String response = reader.readLine();

            if (response != null && response.startsWith("OHCE"))
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
    public boolean notifyRequest (String request) {
        if (!isConnected){
            System.out.println("Not connected to any node. Please start connection first");
            return false;
        }

        try{
            String[] parts = request.split("\n");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
            String fullNodeName = parts[0];
            String fullNodeAddress = parts[1];

            // Sending a message to the server at the other end of the socket
            writer.write("NOTIFY? \n" + fullNodeName + "\n" + fullNodeAddress + "\n");
            writer.flush();

            String response = reader.readLine();
            System.out.println(" "+response);

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
    public String nearest(String hashID) {
//        if (!isConnected) {
//            System.out.println("Not connected to any node. Please start connection first.");
//            return null;
//        }
        try {
            writer.write("NEAREST? " + hashID + "\n");
            writer.flush();

            String response = reader.readLine();
            if (response.startsWith("NODES")) {
                int numberOfNodes = Integer.parseInt(response.split(" ")[1]);
                StringBuilder nodesInfo = new StringBuilder();
                nodesInfo.append(response).append("\n"); // Include the "NODES X" line
                for (int i = 0; i < numberOfNodes; i++) {
                    String nodeName = reader.readLine().trim(); // Trim any trailing newlines
                    String nodeAddress = reader.readLine().trim(); // Trim any trailing newlines
                    nodesInfo.append(nodeName).append("\n").append(nodeAddress).append("\n");
                }
                return nodesInfo.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public TreeMap<Integer, List<String[]>> convertStringNodesToMap(String nearestNodesInfo, String key) throws Exception {
        TreeMap<Integer, List<String[]>> map = new TreeMap<>();
        String[] nodeDetails = nearestNodesInfo.split("\n");
        int numNodes = Integer.parseInt(nodeDetails[0].split(" ")[1]);
        // Skip the first line which is "NODES X"
        for (int i = 1; i < numNodes * 2; i += 2) {
            String nodeName = nodeDetails[i];
            String nodeAddress = nodeDetails[i + 1];

            // Calculate distance for the node using HashID functions.
            byte[] nodeHashID = HashID.computeHashID(nodeName + "\n");
            byte[] keyHashId = HashID.computeHashID(key + "\n");
            int distance = HashID.countLeadingMatchingBits(nodeHashID, keyHashId);

            // Ensure the list for this distance exists and add the node details
            map.computeIfAbsent(distance, k -> new ArrayList<>()).add(new String[]{nodeName, nodeAddress});
        }
        return map;
    }
    public static void main(String[] args) throws IOException {
        TemporaryNode tNode = new TemporaryNode();

        System.out.println("\n===================\n");
        System.out.println("Start: ");
        tNode.start("mohammed.siddiqui@city.ac.uk:2D#4Impl,0.1,FullNode,0","127.0.0.1:6969");

/*
        System.out.println("\n===================\n");
        System.out.println("Store: ");
        tNode.store("Aram\n","The\nKing!");


        System.out.println("\n===================\n");
        System.out.println("Get: ");
        tNode.get("Aram\n");





        System.out.println("\n===================\n");
        System.out.println("Echo: ");
        tNode.echo();

 */

        System.out.println("\n===================\n");
        System.out.println("Notify: ");
        tNode.notifyRequest("martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-2\n"+ "127.0.0.1:3456");



        System.out.println("\n===================\n");
        System.out.println("Nearest: ");
        String nearestNodes = tNode.nearest("0f003b106b2ce5e1f95df39fffa34c2341f2141383ca46709269b13b1e6b4832");
        System.out.println(nearestNodes);


        System.out.println("\n===================\n");
        System.out.println("End: ");
        tNode.end("no requests!");


    }
}
