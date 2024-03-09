// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Aram Gholikimilan
// 220036178
// Aram.Gholikimilan@city.ac.uk


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends


public class TemporaryNode implements TemporaryNodeInterface {
    private String startingNodeHost; // Store the starting node host for potential later use
    private int startingNodePort; // Store the starting node port for potential later use
    private boolean isConnected = false; // Keep track of the connection state
    public boolean start(String startingNodeName, String startingNodeAddress) {

        // Assuming startingNodeAddress is "host:port"
        String[] parts = startingNodeAddress.split(":");
        try (Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("START " + startingNodeName); // Protocol-specific message
            String response = in.readLine();
            return response != null && response.startsWith("ACK"); // Assume ACK is positive
        } catch (Exception e) {
            System.err.println("Start failed: " + e.getMessage());
            return false;
        }
	// Implement this!
	// Return true if the 2D#4 network can be contacted
	// Return false if the 2D#4 network can't be contacted
	//return true;

        /*
            // Split the startingNodeAddress into host and port
            String[] parts = startingNodeAddress.split(":");
            if (parts.length != 2) {
                System.err.println("Invalid startingNodeAddress format. Expected 'host:port'.");
                return false;
            }

            startingNodeHost = parts[0];
            startingNodePort = Integer.parseInt(parts[1]);

            try (Socket socket = new Socket(startingNodeHost, startingNodePort);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Send the START command with the node name
                out.println("START " + startingNodeName);
                String response = in.readLine();

                // Check for a positive acknowledgment
                if (response != null && response.startsWith("ACK")) {
                    isConnected = true; // Update the connection state
                    return true;
                } else {
                    System.err.println("Did not receive expected ACK response. Received: " + response);
                    return false;
                }
            } catch (Exception e) {
                System.err.println("Start failed due to an exception: " + e.getMessage());
                return false;
            }

         */
    }

    public boolean store(String key, String value) {
	// Implement this!
	// Return true if the store worked
	// Return false if the store failed
	//return true;
        // Assuming the startingNodeAddress is set during the start process and stored as a class variable
        try (Socket socket = new Socket(startingNodeHost, startingNodePort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("STORE " + key + " " + value); // Send STORE command
            String response = in.readLine(); // Read the response

            return response != null && response.startsWith("ACK"); // Check for ACK response
        } catch (Exception e) {
            System.err.println("Store operation failed: " + e.getMessage());
            return false;
        }
    }

    public String get(String key) {
	// Implement this!
	// Return the string if the get worked
	// Return null if it didn't
	//return "Not implemented";
        // Assuming the startingNodeAddress is set during the start process and stored as a class variable
        try (Socket socket = new Socket(startingNodeHost, startingNodePort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET " + key); // Send GET command
            String response = in.readLine(); // Read the response

            if (response != null && response.startsWith("VALUE")) {
                return response.substring(6); // Assuming the format "VALUE <value>"
            } else {
                return null; // Value not found or other error
            }
        } catch (Exception e) {
            System.err.println("Get operation failed: " + e.getMessage());
            return null;
        }
    }
}
