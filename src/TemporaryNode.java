// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Aram Gholikimilan
// 220036178
// Aram.Gholikimilan@city.ac.uk


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

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
    private String startingNodeName;
    private String startingNodeHost; // Store the starting node host for potential later use
    private int startingNodePort; // Store the starting node port for potential later use
    private boolean isConnected = false; // Keep track of the connection state
    public boolean start(String startingNodeName, String startingNodeAddress) {
        // Implement this!
        // Return true if the 2D#4 network can be contacted
        // Return false if the 2D#4 network can't be contacted
        //return true;
            this.startingNodeName=startingNodeName;
        try {
            String[] parts = startingNodeAddress.split(":");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
            startingNodeHost = parts[0];
            startingNodePort = Integer.parseInt(parts[1]);

            Socket socket = new Socket(startingNodeHost, startingNodePort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send START message
            out.println("START 1 " + startingNodeName);

            // Wait for START message back
            String response = in.readLine();
            if (response != null && response.startsWith("START"))
            {
                isConnected = true; // Update connection status
                return true;
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("Connection attempt failed: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return false;
    }

    public boolean store(String key, String value) {
	// Implement this!
	// Return true if the store worked
	// Return false if the store failed
	//return true;
        if (!isConnected) {
            System.err.println("Not connected to any node. Please start connection first.");
            return false;
        }


        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try{
            // Append new line if not present
            if (!key.endsWith("\n")) key += "\n";
            if (!value.endsWith("\n")) value += "\n";

            // Count the number of lines in both key and value
            int keyLines = key.split("\n").length;
            int valueLines = value.split("\n", -1).length - 1; // Adjusted to correctly handle the last newline

            String closestNodeAddress = findClosestNode(key);
            if (closestNodeAddress == null) {
                System.err.println("Failed to find the closest node for storing.");
                return false;
            }

            // find the closest node using the keyHashID
            // connect directly to a known node
            socket = connectToNode("Dynamic Node", closestNodeAddress); //startingNodeName, startingNodeHost + ":" + startingNodePort
            if (socket == null) {
                System.out.println("Connection to node failed.");
                return false;
            }

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // sending START message (protocol version 1)
            out.println("START 1 " + this.startingNodeName);
            String startResponse = in.readLine();

            // check for START acknowledgment
            if(startResponse == null || !startResponse.startsWith("START")){
                System.err.println("Failed to start communication with the closest node.");
                return false;
            }

            // format and send PUT? request
            out.println("PUT? " + keyLines + " " + valueLines);
            out.print(key);
            out.print(value);

            //Await and handle response
            String response = in.readLine();
            return "SUCCESS".equals(response);
        } catch (Exception e){
            System.out.println("Error during PUT? request handling (Store operation): " + e.getMessage());
            e.printStackTrace();
        } finally {
            try{
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e){
                System.out.println("Failed to close: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    public String get(String key) {
	// Implement this!
	// Return the string if the get worked
	// Return null if it didn't
	//return "Not implemented";
        if (!isConnected){
            System.out.println("Not connected to any node. Please start connection first");
            return null;
        }

        // ensure the key ends with a newline
        if (!key.endsWith("\n")) key += "\n";

        String closestNodeAddress = findClosestNode(key);
        if (closestNodeAddress == null) {
            System.err.println("Failed to find the closest node for retrieving.");
            return null;
        }

        try(Socket socket = connectToNode("Dynamic Node", closestNodeAddress); //startingNodeName, startingNodeHost + ":" + startingNodePort
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            if (socket == null){
                System.out.println("Connection to node failed.");
                return null;
            }

            // sending START message to conform to protocol requirements before making any request
            out.println("START 1" + this.startingNodeName);
            String startResponse = in.readLine();
            if (startResponse == null || !startResponse.startsWith("START")){
                System.out.println("Failed to start communication properly.");
                return null;
            }

            // sending the GET? request
            int keyLines = key.split(":").length;
            out.println("GET? " + keyLines);
            out.print(key); // send the key

            // await and handle response
            String responseType = in.readLine();
            if (responseType != null && responseType.startsWith("VALUE")){
                int valueLines = Integer.parseInt(responseType.split(" ")[1]);
                StringBuilder value = new StringBuilder();
                for (int i=0; i<valueLines;i++){
                    value.append(in.readLine());
                    if (i < valueLines - 1){
                        value.append("\n");
                    }
                }
                return value.toString();
            } else if ("NOPE".equals(responseType)) {
                return null;
            } else {
                System.out.println("Unexpected response: " + responseType);
            }
        } catch (Exception e){
            System.out.println("Error during GET? request handling: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Connects to a node in the 2D#4 network.
     *
     * @param nodeName The name of the node to connect to (not used in the connection itself, but might be useful for logging or future extensions).
     * @param nodeAddress The address of the node, in the format "ipOrDnsName:portNumber".
     * @return A Socket connected to the specified node, or null if the connection could not be established.
     */
    private Socket connectToNode(String nodeName, String nodeAddress) {
        // Split the nodeAddress into IP/DNS name and port number
        String[] parts = nodeAddress.split(":");
        if (parts.length != 2) {
            System.err.println("Invalid node address format: " + nodeAddress);
            return null;
        }

        String ipOrDnsName = parts[0];
        int portNumber;
        try {
            portNumber = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number in node address: " + nodeAddress);
            return null;
        }

        try {
            // Attempt to create a socket and connect to the node
            Socket socket = new Socket(ipOrDnsName, portNumber);
            System.out.println("Successfully connected to node: " + nodeName + " at address: " + nodeAddress);
            return socket;
        } catch (IOException e) {
            System.err.println("Failed to connect to node: " + nodeName + " at address: " + nodeAddress);
            e.printStackTrace();
        }

        /*
        try {
            String[] parts = nodeAddress.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid node address format.");
            }
            int port = Integer.parseInt(parts[1]);
            return new Socket(parts[0], port);
        } catch (IOException e) {
            System.err.println("Failed to connect to node: " + nodeName + " due to: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
         */

        return null;
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


    private String findClosestNode(String key) {
        if (!key.endsWith("\n")) key += "\n"; // Ensure newline at the end

        String keyHashIDHex;
        try {
            byte[] keyHashID = HashID.computeHashID(key); // Compute hashID
            keyHashIDHex = bytesToHex(keyHashID); // Convert hashID to hex string
        } catch (Exception e) {
            System.err.println("Failed to compute hashID: " + e.getMessage());
            return null;
        }

        try (Socket socket = connectToNode(startingNodeName, startingNodeHost + ":" + startingNodePort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            if (socket == null) {
                System.err.println("Connection to node failed for NEAREST? query.");
                return null;
            }

            // START protocol initiation
            out.println("START 1 " + this.startingNodeName);
            String response = in.readLine();
            if (response == null || !response.startsWith("START")) {
                System.err.println("Failed to start communication properly for NEAREST? query.");
                return null;
            }

            // Send NEAREST? request with the key's hashID
            out.println("NEAREST? " + keyHashIDHex);

            response = in.readLine();
            if (response != null && response.startsWith("NODES")) {
                int numberOfNodes = Integer.parseInt(response.split(" ")[1]);
                if (numberOfNodes > 0) {
                    String closestNodeName = null;
                    String closestNodeAddress = null;
                    // For simplicity, choose the first node returned as the closest
                    // More sophisticated selection could be implemented here
                    for (int i = 0; i < numberOfNodes; i++) {
                        String nodeName = in.readLine(); // Read node name
                        String nodeAddress = in.readLine(); // Read node address
                        if (i == 0) { // Select the first node as closest for simplicity
                            closestNodeName = nodeName;
                            closestNodeAddress = nodeAddress;
                        }
                    }
                    System.out.println("Closest node found: " + closestNodeName + " at " + closestNodeAddress);
                    return closestNodeAddress; // Return the address of the closest node
                }
            }
        } catch (IOException e) {
            System.err.println("IOException during NEAREST? query: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number of nodes: " + e.getMessage());
        }

        return null;
    }

}
