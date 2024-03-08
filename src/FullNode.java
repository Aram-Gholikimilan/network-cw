// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Aram Gholikimilan
// 220036178
// Aram.Gholikimilan@city.ac.uk


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {

    public boolean listen(String ipAddress, int portNumber) {
	// Implement this!
	// Return true if the node can accept incoming connections
	// Return false otherwise

	//return true;

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
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
	// Implement this!
	//return;
        try {
            // Extract host and port from startingNodeAddress (assuming "host:port" format)
            String[] parts = startingNodeAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            // Establish connection to the starting node
            try (Socket socket = new Socket(host, port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Send a protocol-specific message to the starting node
                // Example: JOIN [ThisNodeName] [ThisNodeAddress]
                // Adapt this based on your protocol
                out.println("JOIN " + startingNodeName + " " + host + ":" + port);

                // Wait for a response or perform additional setup as required by the protocol
                String response = in.readLine();
                System.out.println("Response from starting node: " + response);
            }
        } catch (Exception e) {
            System.err.println("Error connecting to the starting node: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleClientConnection(Socket clientSocket) {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read the request from the client
            String request = input.readLine();
            // Assuming the protocol includes a way to differentiate request types
            // Example: "GET key", "STORE key value"
            if (request != null) {
                System.out.println("Received request: " + request);
                // Process the request based on the protocol
                // For simplicity, let's assume we just echo back the request
                output.println("ECHO: " + request);
            }

            // Close the connection after handling the request
            clientSocket.close();
        } catch (Exception e) {
            System.err.println("Error handling client connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
