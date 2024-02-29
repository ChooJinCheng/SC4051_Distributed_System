package message;

import models.MonitorClient;

import java.net.InetAddress;

public class MonitorMessage extends BaseMessage{
    private MonitorClient monitorClient;

    public MonitorMessage() {
        super();
    }

    public MonitorClient getMonitorClient() {
        return monitorClient;
    }

    public void setMonitorClient(MonitorClient monitorClient) {
        this.monitorClient = monitorClient;
    }
}
