package models;


public class MonitorClient {
    private String clientAddress;
    private int clientPort;
    private int monitorInterval;

    public MonitorClient (){

    }
    public MonitorClient(String clientAddress, int clientPort, int monitorInterval) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.monitorInterval = monitorInterval;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public int getMonitorInterval() {
        return monitorInterval;
    }

    public void setMonitorInterval(int monitorInterval) {
        this.monitorInterval = monitorInterval;
    }
}
