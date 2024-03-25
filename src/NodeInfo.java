import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeInfo {
    private String nodeName;
    private String nodeHost;
    private String nodeIp;
    private String nodePort;
    private int distance;
    private String time;
    private String nodeAddress;

    public NodeInfo(String nodeName,String nodeAddress, String time) throws UnknownHostException {
        this.nodeName = nodeName;
        this.nodeAddress = nodeAddress;
        String[] parts = nodeAddress.split(":");
        //this.nodeIp = parts[0];
        this.nodeHost = String.valueOf(InetAddress.getByName(parts[0]));
        this.nodePort = String.valueOf(Integer.parseInt(parts[1]));
        //this.distance = distance;
        this.time = time;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public String getNodePort() {
        return nodePort;
    }

    public int getDistance() {
        return distance;
    }

    public String getTime() {
        return time;
    }
}
