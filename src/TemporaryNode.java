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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
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
    private Socket clientSocket;
    private BufferedReader reader;
    private Writer writer;
    private String startingNodeName;
    private String startingNodeAddress;
    private InetAddress startingNodeHost; // Store the starting node host for potential later use
    private int startingNodePort; // Store the starting node port for potential later use
    private boolean isConnected = false; // Keep track of the connection state
    public boolean start(String startingNodeName, String startingNodeAddress) {
        // Implement this!
        // Return true if the 2D#4 network can be contacted
        // Return false if the 2D#4 network can't be contacted
        //return true;
            this.startingNodeName=startingNodeName;
            this.startingNodeAddress=startingNodeAddress;
            try{
                String[] parts = startingNodeAddress.split(":");
                if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
                String IPAddressString = parts[0];
                startingNodeHost = InetAddress.getByName(IPAddressString);
                startingNodePort = Integer.parseInt(parts[1]);

                System.out.println("TCPClient connecting to " + startingNodeAddress);
                //System.out.println(startingNodeHost.toString() + "  :  "+startingNodePort);
                clientSocket = new Socket(startingNodeHost, startingNodePort);
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new OutputStreamWriter(clientSocket.getOutputStream());

                // Sending a message to the server at the other end of the socket
                System.out.println("Sending a message to the server");
                System.out.println(startingNodeName);
                writer.write("START 1 " + startingNodeName +"\n");
                writer.flush();

                String response = reader.readLine();
                System.out.println("The server said : " + response);

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

    public boolean store(String key, String value) {
	// Implement this!
	// Return true if the store worked
	// Return false if the store failed
	//return true;
        if (!isConnected) {
            System.err.println("Not connected to any node. Please start connection first.");
            return false;
        }

        try{
            // Append new line if not present
            if (!key.endsWith("\n")) key += "\n";
            if (!value.endsWith("\n")) value += "\n";

            // Count the number of lines in both key and value
            int keyLines = key.split("\n").length;
            int valueLines = value.split("\n", -1).length - 1; // Adjusted to correctly handle the last newline

            // you have the host and port from start
            System.out.println("TCPClient connecting to " + startingNodeAddress);
            //Socket clientSocket = new Socket(startingNodeHost, startingNodePort);
            //BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // Sending a message to the server at the other end of the socket
            System.out.println("Sending a message to the server");
            writer.write("PUT? " + keyLines + " " + valueLines + "\n"); //  + "\n" + key + "\n" + value
            writer.write(key);
            writer.write(value+"\n");
            System.out.println("the value in temp: \n"+value);
            writer.flush();

            String response = reader.readLine();
            System.out.println("The server said : " + response);

            if (response != null && response.startsWith("SUCCESS"))
            {
                isConnected = true; // Update connection status
                return true;
            }

        } catch (Exception e){
            System.out.println("Error during PUT? request handling (Store operation): "+e.getMessage());
            e.printStackTrace();
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

        try{
            // ensure the key ends with a newline
            if (!key.endsWith("\n")) key += "\n";
            // Count the number of lines in both key and value
            int keyLines = key.split("\n").length;

            // you have the host and port from start
            System.out.println("TCPClient connecting to " + startingNodeAddress);
            //Socket clientSocket = new Socket(startingNodeHost, startingNodePort);
            //BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // Sending a message to the server at the other end of the socket
            System.out.println("Sending a message to the server");
            writer.write("GET? " + keyLines + "\n"+key+"\n");
            writer.flush();

            String response = reader.readLine();
            System.out.println("The server said : " + response);
            //String response2 = reader.readLine();
            //System.out.println("The server said2 : " + response2);

            if (response != null && response.startsWith("VALUE"))
            {
                return response;
            }

        } catch (Exception e){
            System.out.println("Error during GET? request handling: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void end (String reason){

        try{
            System.out.println("TCPClient connecting to " + startingNodeAddress);
            System.out.println(startingNodeHost.toString() + "  :  "+startingNodePort);
            //Socket clientSocket = new Socket(startingNodeHost, startingNodePort);
            //BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

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
    public boolean echo (){

        if (!isConnected){
            System.out.println("Not connected to any node. Please start connection first");
            return false;
        }

        try{
            System.out.println("TCPClient connecting to " + startingNodeAddress);
            System.out.println(startingNodeHost.toString() + "  :  "+startingNodePort);
            //Socket clientSocket = new Socket(startingNodeHost, startingNodePort);
            //BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // Sending a message to the server at the other end of the socket
            System.out.println("Sending a message to the server");
            writer.write("ECHO? " +"\n");
            writer.flush();

            String response = reader.readLine();
            System.out.println("The server said : " + response);

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

            System.out.println("TCPClient connecting to " + startingNodeAddress);
            System.out.println(startingNodeHost.toString() + "  :  "+startingNodePort);

            // Sending a message to the server at the other end of the socket
            System.out.println("Sending a message to the server");
            writer.write("NOTIFY? \n" + fullNodeName + "\n" + fullNodeAddress + "\n");
            writer.flush();

            String response = reader.readLine();
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
    public String nearest (String hash){// the string is a hashID written in hex
        if (!isConnected){
            System.out.println("Not connected to any node. Please start connection first");
            return null;
        }
        try{
            // Sending a message to the server at the other end of the socket
            System.out.println("Sending a message to the server");
            writer.write("NEAREST? ");
            writer.write(hash + "\n");
            writer.flush();

            String response = reader.readLine();
            System.out.println("The server said : " + response);

            if (response != null && response.startsWith("NODES"))
            {
                isConnected = true; // Update connection status
                return response;
            }

        } catch (Exception e){
            System.out.println("Connecting attempt failed: " + e.getMessage());
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

    public static void main(String[] args) throws IOException {
        TemporaryNode tNode = new TemporaryNode();

        System.out.println("\n===================\n");
        System.out.println("Start: ");
        tNode.start("aram.gholikimilan@city.ac.uk:MyCoolImplementation,1.41,test-node-2","127.0.0.1:3456");

        System.out.println("\n===================\n");
        System.out.println("Store: ");
        tNode.store("Welcome",
                    "Hello\n" +
                    "World!");


        System.out.println("\n===================\n");
        System.out.println("Get: ");
        tNode.get("Welcome");
/*
        System.out.println("\n===================\n");
        System.out.println("Echo: ");
        tNode.echo();

        System.out.println("\n===================\n");
        System.out.println("Notify: ");
        tNode.notifyRequest("martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-2\n"+ "127.0.0.1:3456");

        System.out.println("\n===================\n");
        System.out.println("Nearest: ");
        tNode.nearest("0f003b106b2ce5e1f95df39fffa34c2341f2141383ca46709269b13b1e6b4832");


        System.out.println("\n===================\n");
        System.out.println("End: ");
        tNode.end("no requests!");


 */
    }
}
