package message;

import models.MonitorClient;

/*
 * MonitorMessage class contains the information needed for monitoring a file such as ClientAddress, ClientPort and the interval of monitoring all these information
 * is encapsulated within the MonitorClient class
 */
public class MonitorMessage extends BaseMessage{

    private MonitorClient monitorClient;

    public MonitorMessage() {
        super();
    }
    public MonitorMessage(String commandType, String filePath, String content,
                          MonitorClient monitorClient) {
        super(commandType, filePath, content);
        this.monitorClient = monitorClient;
    }

    @Override
    public String toString() {
        return "\t\t" + super.toString() + this.monitorClient
                + "\n\t\tContent: " + super.getContent();
    }

    public MonitorClient getMonitorClient() {
        return monitorClient;
    }

    public void setMonitorClient(MonitorClient monitorClient) {
        this.monitorClient = monitorClient;
    }
}
