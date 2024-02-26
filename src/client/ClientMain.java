package client;

import utilities.PropertyUtil;

import java.net.*;
import java.util.Properties;

public class ClientMain {
    public static void main(String[] args) {
        //Init Client Param
        Properties properties = PropertyUtil.getProperty();

        try (DatagramSocket socket = new DatagramSocket()){

            String message = "Hello, server!";
            byte[] sendBuffer = message.getBytes();
            InetAddress serverAddress = InetAddress.getByName(properties.getProperty("SERVER_HOSTNAME"));
            int serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));

            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);
            socket.send(sendPacket);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            String echoedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Received from server: " + echoedMessage);

            //socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
