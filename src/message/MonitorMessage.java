package message;

import models.MonitorClient;

import java.net.InetAddress;

public class MonitorMessage extends BaseMessage{

    private MonitorClient monitorClient;

    public MonitorMessage() {
        super();
    }
    public MonitorMessage(int requestID, String commandType, String filePath, String content,
                          MonitorClient monitorClient) {
        super(requestID, commandType, filePath, content);
        this.monitorClient = monitorClient;
    }

    public MonitorClient getMonitorClient() {
        return monitorClient;
    }

    public void setMonitorClient(MonitorClient monitorClient) {
        this.monitorClient = monitorClient;
    }
}
