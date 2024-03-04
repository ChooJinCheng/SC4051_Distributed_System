package message;

import models.MonitorClient;

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

    public MonitorClient getMonitorClient() {
        return monitorClient;
    }

    public void setMonitorClient(MonitorClient monitorClient) {
        this.monitorClient = monitorClient;
    }
}
