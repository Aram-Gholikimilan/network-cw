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
import java.util.Arrays;

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

                //System.out.println("TCPClient connecting to " + startingNodeAddress);
                //System.out.println(startingNodeHost.toString() + "  :  "+startingNodePort);
                clientSocket = new Socket(startingNodeHost, startingNodePort);
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new OutputStreamWriter(clientSocket.getOutputStream());

                // Sending a message to the server at the other end of the socket
//                System.out.println("Sending a message to the server");
//                System.out.println(startingNodeName);
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

        return false;
    }

    public boolean store(String key, String value) {
	// Implement this!
	// Return true if the store worked
	// Return false if the store failed
	//return true;

        try {
            // Append new line if not present
//            if (!key.endsWith("\n")) key += "\n";
//            if (!value.endsWith("\n")) value += "\n";

            // Count the number of lines in both key and value
            int keyLines = key.split("\n").length;
            int valueLines = value.split("\n").length; // Adjusted to correctly handle the last newline

            // Sending a message to the server at the other end of the socket
            System.out.println("Sending a message to the server");
            writer.write("PUT? " + keyLines + " " + valueLines + "\n" + key + value); //  + "\n" + key + "\n" + value
//            System.out.println("the value in temp: \n" + value);
            writer.flush();

            String response = reader.readLine();
           // System.out.println("The server said : " + response);

            if (response != null && response.startsWith("SUCCESS")) {
                //isConnected = true; // Update connection status
                return true;
            } else if (response.startsWith("FAILED")) {
                // Get the hash ID for the key to find nearest nodes
                byte[] keyHash = HashID.computeHashID(key);
                String hexKeyHash = HashID.bytesToHex(keyHash);

                // Call nearest to find nearest nodes
                String nearestNodesInfo = nearest(hexKeyHash);
                System.out.println("nearest nodes: \n" + nearestNodesInfo);
                //System.out.println(nearestNodesInfo);
                if (nearestNodesInfo == null || nearestNodesInfo.isEmpty()) {
                    System.err.println("Failed to retrieve nearest nodes or none are available.");
                    return false;
                }


                // Split the nearestNodesInfo to get individual node details
                String[] nodeDetails = nearestNodesInfo.split("\n");
                int numNodes = Integer.parseInt(nodeDetails[0].split(" ")[1]);
                // Skip the first line which is "NODES X"
                for (int i = 1; i < numNodes; i+=2) {
                    String nodeName = nodeDetails[i];
                    String nodeAddress = nodeDetails[i+1];

                    // Attempt to store on the nearest node
                    if (attemptStoreOnNode(nodeName, nodeAddress, key, value)) {
                        System.out.println("Successfully stored on fallback node: " + nodeName);
                        return true;
                    }
                }

                String nodeName = nodeDetails[1];
                String nodeAddress = nodeDetails[2];
                attemptStoreOnNode(nodeName,nodeAddress,key,value);

                System.err.println("Failed to store the key-value pair on any fallback node.");
                return false;
            }

        } catch (Exception e){
            System.out.println("Error during PUT? request handling (Store operation): "+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private boolean attemptStoreOnNode(String nodeName, String nodeAddress, String key, String value) {
        try {
            // Split the address to get IP and port
            String[] addressParts = nodeAddress.split(":");
            String ip = addressParts[0];
            int port = Integer.parseInt(addressParts[1]);

            // Open a new connection to the node
            Socket socket = new Socket(ip, port);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Writer writer = new OutputStreamWriter(socket.getOutputStream());

            // Initiate protocol communication, e.g., send START command
            writer.write("START 1 " + this.startingNodeName + "\n"); // Adjust as needed
            writer.flush();

            // Wait for a START response if necessary
            // Assume the node responds with START, indicating ready to communicate
            reader.readLine(); // Read the response to the START command

            // Now send the PUT? request
            writer.write("PUT? " + key.split("\n").length + " " + (value.split("\n", -1).length - 1) + "\n" + key + value);
            writer.flush();

            // Read and check the response
            String response = reader.readLine();
            socket.close(); // Always close the socket

            // Check if the response indicates success
            return "SUCCESS".equals(response);
        } catch (Exception e) {
            System.err.println("Error attempting to store on node " + nodeName + ": " + e.getMessage());
            return false;
        }
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

        int min=99999;
        String minNodeName="";
        String minNodeAddress="";
        int count = 0;
        try{
            while(true){
                count++;
                System.out.println("get: "+count);
                // ensure the key ends with a newline
                //if (!key.endsWith("\n")) key += "\n";
                // Count the number of lines in both key and value
                int keyLines = key.split("\n").length;

                // you have the host and port from start
                //System.out.println("TCPClient connecting to " + startingNodeAddress);
                //Socket clientSocket = new Socket(startingNodeHost, startingNodePort);
                //BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                //Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

                //String threeClosestNodes = nearest(key);
                // Sending a message to the server at the other end of the socket
                //System.out.println("Sending a message to the server");
                writer.write("GET? " + keyLines + "\n"+key);
                writer.flush();

                String response = reader.readLine();
                System.out.println("the response, sdfof: "+ response);
                //String[] parts = response.split(" ", 1);
                if (response.startsWith("VALUE"))
                {
                    //int valueLinesCount = Integer.parseInt(parts[1]);
                    int valueLinesCount2 = Integer.parseInt(response.split(" ")[1]);
                    StringBuilder valueBuilder = new StringBuilder();
                    valueBuilder.append(response).append("\n");
                    for (int i = 0; i < valueLinesCount2; i++) {
                        valueBuilder.append(reader.readLine()).append("\n");
                    }

                    String valueResponse = valueBuilder.toString();

                    System.out.println("The sdjcbshkhgsraubserver said : \n" + valueResponse); //valueResponse
                    //String response2 = reader.readLine();
                    //System.out.println("The server said2 : " + response2);


                    return valueResponse;

                } else if (response.startsWith("NOPE")) {
                    int countNopes=0;
                    while(true){
                        countNopes++;
                        System.out.println("nopes: " + countNopes);
                    // Calculate the hashID of the key to find the nearest nodes
                    byte[] keyHashID = HashID.computeHashID(key + "\n");
                    String hexKeyHashID = HashID.bytesToHex(keyHashID);

                    // Get the nearest nodes
                    String nearestNodesInfo = nearest(hexKeyHashID);
                    System.out.println("nearest nodes: \n" + nearestNodesInfo);
                    if (nearestNodesInfo == null || nearestNodesInfo.isEmpty()) {
                        System.err.println("Failed to retrieve nearest nodes or none are available.");
                        return null;
                    }
                    // System.out.println("HERE: " + nearestNodesInfo);

                    // Parse the nearestNodesInfo to extract node details
                    String[] lines = nearestNodesInfo.split("\n");
                    int numNodes = Integer.parseInt(lines[0].split(" ")[1]);

                    // System.out.println(Arrays.toString(lines));
                    // Skip the first line which is "NODES X"

//                for (int i = 1; i < numNodes; i += 2) {
//                    String nodeName = lines[i]; // Node name
//                    String nodeAddress = lines[i + 1]; // Node address
//                    System.out.println(nodeName + "\n" + nodeAddress);
//                    System.out.println("value " + i);
//                    // Attempt to get from the nearest node
//                    String value = attemptGetFromNode(nodeName, nodeAddress, key);
//                    if (value != null && !value.equals("NOPE")) {
//                        System.out.println("Successfully retrieved value from fallback node: " + nodeName);
//                        return value; // Successfully retrieved value from a fallback node
//                    }
//                    System.out.println("i: " + i);
//                }
                    // Adjusted loop to start from the first node's information
                    for (int i = 1; i < numNodes * 2; i += 2) {
                        String nodeName = lines[i]; // Adjust index for node name
                        String nodeAddress = lines[i + 1]; // Adjust index for node address
                        System.out.println(minNodeName);

                        byte[] nodeHashID = HashID.computeHashID(nodeName + "\n");
                        byte[] keyHashId = HashID.computeHashID(key + "\n");
                        int distance = HashID.calculateDistance(nodeHashID, keyHashId);
                        if (distance < min) {
                            min = distance;
                            minNodeName = nodeName;
                            minNodeAddress = nodeAddress;
                        }
                        System.out.println(nodeName + ", distance: " + distance);
                    }


                    System.out.println("min node name: " + minNodeName);

                    clientSocket.close();
                    reader.close();
                    writer.close();

                    InetAddress host = InetAddress.getByName(minNodeAddress.split(":")[0]);
//                    int port = Integer.parseInt(minNodeAddress.split(":")[1]);
//                    Socket clientSocket = new Socket(host, port);
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//                    Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

                    this.startingNodeAddress = minNodeAddress;
                    this.startingNodeName = minNodeName;
                    start(minNodeName, minNodeAddress);

//                    writer.write("START 1 " + name +"\n");
//                    writer.flush();

                    String value = attemptGetFromNode(minNodeName, minNodeAddress, key);
                    //System.out.println(value);
                    if (value != null && !value.equals("NOPE")) {
                        System.out.println("Successfully retrieved value from fallback node: " + minNodeName);
                        return value;
                    }
                }
//                String nodeName = lines[1];
//                String nodeAddress = lines[2];
//                attemptGetFromNode(nodeName,nodeAddress,key);




//                    System.err.println("Failed to retrieve the key-value pair from any fallback node.");
//                    return null;
                }
            }

        } catch (Exception e){
            System.out.println("Error during GET? request handling: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private String attemptGetFromNode(String nodeName, String nodeAddress, String key) {
        try {
            System.out.println("LOOK: " + nodeAddress);
            // Split the address to get IP and port
            String[] addressParts = nodeAddress.split(":");
            InetAddress ip = InetAddress.getByName(addressParts[0]);
            int port = Integer.parseInt(addressParts[1]);

//            clientSocket = new Socket(ip, port);
//            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//            writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // Open a new connection to the node
           // try {

                // Initiate protocol communication, e.g., send START command
//                writer.write("START 1 " + name + "\n");
//                writer.flush();
//                System.out.println("hi!");

                // Wait for a START response if necessary
//                String res = reader.readLine(); // Assume the node responds
//                System.out.println("result of start: \n"+res);
//                // Now send the GET? request
                writer.write("GET? " + key.split("\n").length + "\n" + key);
                writer.flush();

                // Read and process the response
                String response = reader.readLine();
                System.out.println("get message 2: " + response);

                if (response.startsWith("VALUE")) {
                    StringBuilder valueBuilder = new StringBuilder(response);
                    String[] parts = response.split(" ");
                    System.out.println("l:" + parts[0]);
                    int lines = Integer.parseInt(parts[1]);
                    for (int i = 0; i < lines; i++) {
                        valueBuilder.append("\n").append(reader.readLine());
                    }
                    return valueBuilder.toString();
                }
                //return "NOPE"; // hereee

            } catch (Exception e) {
                System.err.println("Error attempting to get from node " + nodeName + ": " + e.getMessage());
            }
            return "NOPE";
      //  } catch (UnknownHostException e) {
        //    throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
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
            writer.write("ECHO?" +"\n");
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

    public String nearest(String hashID) {
        if (!isConnected) {
            System.out.println("Not connected to any node. Please start connection first.");
            return null;
        }

        try {
            System.out.println("writer: "+writer.toString());
            writer.write("NEAREST? " + hashID + "\n");
            writer.flush();

            String response = reader.readLine();
        //    System.out.println("RESP: " + response);
            if (response.startsWith("NODES")) {
                int numberOfNodes = Integer.parseInt(response.split(" ")[1]);
                StringBuilder nodesInfo = new StringBuilder();
                nodesInfo.append(response).append("\n"); // Include the "NODES X" line
                for (int i = 0; i < numberOfNodes; i++) {
                    String nodeName = reader.readLine().trim(); // Trim any trailing newlines
                    String nodeAddress = reader.readLine().trim(); // Trim any trailing newlines
                    nodesInfo.append(nodeName).append("\n").append(nodeAddress).append("\n");
                }
        //        System.out.println(nodesInfo);
                // Print the complete nodes information for debugging before returning
//                System.out.println("Complete nodes information received:\n" + nodesInfo);
                return nodesInfo.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null; // hereee
    }


/*
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

            String[] parts = response.split(" ", 2);

            System.out.println(Arrays.toString(parts));
            int nodesCount = Integer.parseInt(parts[1]);
            System.out.println(nodesCount);
            StringBuilder valueBuilder = new StringBuilder();
            valueBuilder.append(response).append("\n");
            for (int i = 0; i < nodesCount ; i++) {
                valueBuilder.append(reader.readLine()).append("\n");
                valueBuilder.append(reader.readLine()).append("\n");
                System.out.println(i+":"+valueBuilder);
            }

            String valueResponse = valueBuilder.toString();
            System.out.println("last: "+valueBuilder);

            System.out.println("The server said : \n" + valueResponse);


            if (response.startsWith("NODES"))
            {
                isConnected = true; // Update connection status
                return valueResponse;
            }

        } catch (Exception e){
            System.out.println("Connecting attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
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

    public static void main(String[] args) throws IOException {
        TemporaryNode tNode = new TemporaryNode();

        System.out.println("\n===================\n");
        System.out.println("Start: ");
        tNode.start("aram.gholikimilan@city.ac.uk:MyCoolImplementation,1.41,test-node-2","127.0.0.1:6969");

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

        System.out.println("\n===================\n");
        System.out.println("Notify: ");
        tNode.notifyRequest("martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-2\n"+ "127.0.0.1:3456");


 */
        System.out.println("\n===================\n");
        System.out.println("Nearest: ");
        String nearestNodes = tNode.nearest("0f003b106b2ce5e1f95df39fffa34c2341f2141383ca46709269b13b1e6b4832");
        System.out.println(nearestNodes);

        /*
        System.out.println("\n===================\n");
        System.out.println("End: ");
        tNode.end("no requests!");


 */


    }
}
