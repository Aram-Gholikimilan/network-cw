import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeInfo {
    private String nodeName;
    private String nodeHost;
    private String nodePort;
    private String time;
    private String nodeAddress;

    public NodeInfo(String nodeName,String nodeAddress, String time) throws UnknownHostException {
        this.nodeName = nodeName;
        this.nodeAddress = nodeAddress;
        String[] parts = nodeAddress.split(":");
        this.nodeHost = String.valueOf(InetAddress.getByName(parts[0]));
        this.nodePort = String.valueOf(Integer.parseInt(parts[1]));
        this.time = time;
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
    public String getTime() {
        return time;
    }
}
