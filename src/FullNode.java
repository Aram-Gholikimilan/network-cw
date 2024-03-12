// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Aram Gholikimilan
// 220036178
// Aram.Gholikimilan@city.ac.uk


import java.io.*;
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

    //   Full nodes MUST act
    //   as TCP servers and accept incoming connections using the IP address
    //   and port number in their address.  Full nodes MAY act as TCP clients.

    public FullNode() {}
    private static boolean actServer = true;
    public static void main(String[] args) throws IOException {

        // Act as server:
        if (actServer) {

            // IP Addresses will be discussed in detail in lecture 4
            String IPAddressString = "127.0.0.1";
            InetAddress host = InetAddress.getByName(IPAddressString);

            // Port numbers will be discussed in detail in lecture 5
            int port = 4567;

            // The server side is slightly more complex
            // First we have to create a ServerSocket
            System.out.println("Opening the server socket on port " + port);
            ServerSocket serverSocket = new ServerSocket(port);

            // The ServerSocket listens and then creates as Socket object
            // for each incoming connection
            System.out.println("Server waiting for client...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected!");

            // Like files, we use readers and writers for convenience
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // We can read what the client has said
            String message = reader.readLine();
            System.out.println("The client said : " + message);

            // Sending a message to the client at the other end of the socket
            System.out.println("Sending a message to the client");
            writer.write("Nice to meet you\n");
            writer.flush();
            // To make better use of bandwidth, messages are not sent
            // until the flush method is used

            // Close down the connection
            clientSocket.close();
        } else {
            // IP Addresses will be discussed in detail in lecture 4
            String IPAddressString = "127.0.0.1";
            InetAddress host = InetAddress.getByName(IPAddressString);

            // Port numbers will be discussed in detail in lecture 5
            int port = 4567;

            // This is where we create a socket object
            // That creates the TCP conection
            System.out.println("TCPClient connecting to " + host.toString() + ":" + port);
            Socket clientSocket = new Socket(host, port);

            // Like files, we use readers and writers for convenience
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // Sending a message to the server at the other end of the socket
            System.out.println("Sending a message to the server");
            writer.write("Hello Server!\n");
            writer.flush();
            // To make better use of bandwidth, messages are not sent
            // until the flush method is used

            // We can read what the server has said
            String response = reader.readLine();
            System.out.println("The server said : " + response);

            // Close down the connection
            clientSocket.close();
        }
    }
}
