package server;

import server.handler.ServerHandler;
import utilities.PropertyUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Properties;


public class ServerMain {
    public static void main(String[] args) {
        //Init Server Param
        Properties properties = PropertyUtil.getProperty();
        int port = Integer.parseInt(properties.getProperty("SERVER_PORT"));

        try (DatagramSocket socket = new DatagramSocket(port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            boolean atLeastOnce = handleInvocationInput(reader);
            String semanticMode = atLeastOnce ? "AtLeastOnce" : "AtMostOnce";

            ServerHandler serverHandler = new ServerHandler(atLeastOnce);
            System.out.println("Server is on invocation semantic " + semanticMode);
            System.out.println("Server is listening on port " + port);

            while (true) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                byte[] requestData = receivePacket.getData();

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                byte[] sendBuffer = serverHandler.processRequestAndGetReply(requestData);
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
                socket.send(sendPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean handleInvocationInput(BufferedReader reader) throws IOException {
        System.out.println("Input invocation semantics (0=AMO, 1=ALO): ");
        String input = reader.readLine();

        while (!input.matches("[01]")) {
            System.out.println("Invalid input. Please enter 0 or 1.");
            input = reader.readLine();
        }

        // Convert input to boolean
        return "1".equals(input);
    }
}

/*String filePath = "test.txt";
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
        fileMonitorService.registerClient(filePath, monitorClient2);*/
