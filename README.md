IN2011 COMPUTER NETWORK
ARAM GHOLIKIMILAN
220036178

the additional class that i have created in my project is "NodeInfo" class, it contains the following:

Class Definition: NodeInfo

    Fields:
        nodeName: Stores the name of the node.
        nodeHost: Stores the hostname derived from the IP address.
        nodePort: Stores the port number as a string.
        time: Stores a timestamp or time-related information.
        nodeAddress: Stores the full address of the node, typically in the format "host:port".

    Constructor:
        The constructor takes three parameters: nodeName, nodeAddress, and time.
        It initializes the nodeName and nodeAddress directly with the provided values.
        It splits nodeAddress into hostname and port using : as a delimiter.
        It resolves the hostname to an IP address using InetAddress.getByName(parts[0]).
        It extracts the port from the nodeAddress and converts it to a string.

    Methods:
        getNodeAddress(): Returns the full node address.
        getNodeName(): Returns the node's name.
        getNodeHost(): Returns the node's hostname.
        getNodePort(): Returns the node's port number as a string.
        getTime(): Returns the timestamp or time information.

The following of the functionalities that work correctly:
The temporary node and full node contains these following features

START : it starts the connection with a node
ECHO : this is for checking if there is a connection
PUT : this is for storing a key value paire
GET : get the value for the given key
NOTIFY : notify other nodes the specific full node with its address
NEAREST : get the three closest nodes from the given key
END : end the connection with the node

when testing the poem jabberwocky retrieval i am able to get all parts (1-7) from all nodes (20000-20010).

when trying to store a key-value in all of the nodes i am able to successfully store in each one (using CmdLineStore).
similarly, when trying to get the value using the key i am able to do so successfully from each node (using CmdLineGet).

the filter that i used for wireshark:
(ip.addr == 10.0.1.8 and tcp.port == 20000) or (ip.addr == 10.0.0.164 and (tcp.port == 20000 or tcp.port == 20001 or tcp.port == 20002 or tcp.port == 20003 or tcp.port == 20004 or tcp.port == 20005 or tcp.port == 20006 or tcp.port == 20007 or tcp.port == 20008 or tcp.port == 20009 or tcp.port == 20010))

All the functionality and features based on the coursework marking scheme and RFC works.
As an evidence the wireshark recording is included.
