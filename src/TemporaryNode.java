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
        ArrayList<String> visitedNodes = new ArrayList<>();
        visitedNodes.add(startingNodeName);
        try {
            while(true) {
                int keyLines = key.split("\n").length;
                int valueLines = value.split("\n").length;

                writer.write("PUT? " + keyLines + " " + valueLines + "\n" + key + value); //  + "\n" + key + "\n" + value
                writer.flush();

                String response = reader.readLine();

                if (response != null && response.startsWith("SUCCESS")) {
                    clientSocket.close();
                    return true;
                } else if (response.startsWith("FAILED")) {
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
                        visitedNodes.add(nodeName);

                        writer.write("PUT? " + keyLines + " " + valueLines + "\n" + key + value); //  + "\n" + key + "\n" + value
                        writer.flush();

                        String response2 = reader.readLine();
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

        ArrayList<String> visitedNodes = new ArrayList<>();
        visitedNodes.add(startingNodeName);
        try {
            while(true) {

                int keyLines = key.split("\n").length;

                writer.write("GET? " + keyLines + "\n" + key);
                writer.flush();

                String response = reader.readLine();

                if (response != null && response.startsWith("VALUE")) {
                    clientSocket.close();

                    String[] parts = response.split(" ", 2);
                    int valueLinesCount = Integer.parseInt(parts[1]);
                    StringBuilder valueBuilder = new StringBuilder();
                    for (int i = 0; i < valueLinesCount; i++) {
                        valueBuilder.append(reader.readLine()).append("\n");
                    }

                    String value = valueBuilder.toString();
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

                    List<String[]> nodeDetails = new ArrayList<>();

                    for (Map.Entry<Integer, List<String[]>> entry : sortedNodes.entrySet()) {
                        // Retrieve the distance (key of the TreeMap) and the associated list of node details
//                        Integer distance2 = entry.getKey();
                        nodeDetails = entry.getValue(); // List of node details arrays

//                        // Print the distance first
//                        System.out.println("Distance: " + distance2);
//
//                        // Iterate over each node detail array in the list for this specific distance
//                        for (String[] details : nodeDetails) {
//                            // Each 'details' array contains the node name at index 0 and the address at index 1
//                            System.out.println("  Node Name: " + details[0] + ", Address: " + details[1]);
//                        }
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
                        String nodeName3 = details[0]; // Node name
                        String nodeAddress3 = details[1]; // Node address

                        end("CANNOT-GET");
                        clientSocket.close();
                        reader.close();
                        writer.close();


                        String[] address = nodeAddress3.split(":");
                        int port = Integer.parseInt(address[1]);
                        InetAddress host = InetAddress.getByName(address[0]);
                        clientSocket = new Socket(host, port);

                        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        writer = new OutputStreamWriter(clientSocket.getOutputStream());


                        writer.write("START 1 " + name + "\n");
                        writer.flush();
                        String r = reader.readLine();
                        visitedNodes.add(nodeName3);

                        writer.write("GET? " + keyLines + "\n" + key);
                        writer.flush();

                        String response2 = reader.readLine();

                        if (response2 != null && response2.startsWith("VALUE")) {
                            clientSocket.close();

                            String[] parts = response2.split(" ", 2);
                            int valueLinesCount2 = Integer.parseInt(parts[1]);
                            StringBuilder valueBuilder = new StringBuilder();
                            for (int i = 0; i < valueLinesCount2; i++) {
                                valueBuilder.append(reader.readLine()).append("\n");
                            }

                            String value2 = valueBuilder.toString();
                            return value2;
                        }
                    }

                    end("CANNOT-GET");
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
                    visitedNodes.add(minNodeName2);

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
        } catch (Exception e){
            System.out.println("Connecting attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public boolean echo (){
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

        try{
            String[] parts = request.split("\n");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
            String fullNodeName = parts[0];
            String fullNodeAddress = parts[1];

            // Sending a message to the server at the other end of the socket
            writer.write("NOTIFY? \n" + fullNodeName + "\n" + fullNodeAddress + "\n");
            writer.flush();

            String response = reader.readLine();

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
}
