package server;

import message.MetaMessage;
import message.MonitorMessage;
import models.Metadata;
import models.MonitorClient;
import utilities.CustomSerializationUtil;
import utilities.FileDataExtractorUtil;


public class ServerMain {
    public static void main(String[] args) {
        String filePath = "test.txt";
        FileMonitorService fileMonitorService = new FileMonitorService();

        MonitorClient monitorClient1 = new MonitorClient();
        monitorClient1.setMonitorInterval(15);
        monitorClient1.setClientPort(4600);
        monitorClient1.setClientAddress("localhost");
        MonitorClient monitorClient2 = new MonitorClient();
        monitorClient2.setMonitorInterval(15);
        monitorClient2.setClientPort(4601);
        monitorClient2.setClientAddress("localhost");
        fileMonitorService.registerClient(filePath, monitorClient1);
        fileMonitorService.registerClient(filePath, monitorClient2);

        //Init Server Param
        /*Properties properties = PropertyUtil.getProperty();

        int port = Integer.parseInt(properties.getProperty("SERVER_PORT"));
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("Server is listening on port " + port);
            while (true) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Received from client: " + message);

                // Echo the message back to the client
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                byte[] sendBuffer = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
                socket.send(sendPacket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
