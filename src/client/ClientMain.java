package client;

import message.BaseMessage;
import utilities.CustomSerializationUtil;
import utilities.PropertyUtil;

import java.net.*;
import java.util.Properties;

public class ClientMain {
    public static void main(String[] args) {
        //Init Client Param
        Properties properties = PropertyUtil.getProperty();

        try (DatagramSocket socket1 = new DatagramSocket(4600); DatagramSocket socket2 = new DatagramSocket(4601)){
            while(true){
                System.out.println("Client1 is listening on port " + 4600);
                System.out.println("Client2 is listening on port " + 4601);
                byte[] receiveBuffer1 = new byte[1024];
                DatagramPacket receivePacket1 = new DatagramPacket(receiveBuffer1, receiveBuffer1.length);
                socket1.receive(receivePacket1);

                BaseMessage baseMessage1 = new BaseMessage();
                CustomSerializationUtil.unmarshal(baseMessage1, receivePacket1.getData());
                System.out.println("Received from socket1: "
                        + "\nContent:" + baseMessage1.getContent()
                        + "\nID:" + baseMessage1.getRequestID());

                byte[] receiveBuffer2 = new byte[1024];
                DatagramPacket receivePacket2 = new DatagramPacket(receiveBuffer2, receiveBuffer2.length);
                socket2.receive(receivePacket2);

                BaseMessage baseMessage2 = new BaseMessage();
                CustomSerializationUtil.unmarshal(baseMessage2, receivePacket2.getData());
                System.out.println("Received from socket2: "
                        + "\nContent:" + baseMessage2.getContent()
                        + "\nID:" + baseMessage2.getRequestID());
            }
            /*String message = "Hello, server!";
            byte[] sendBuffer = message.getBytes();
            InetAddress serverAddress = InetAddress.getByName(properties.getProperty("SERVER_HOSTNAME"));
            int serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));

            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);
            socket.send(sendPacket);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            String echoedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Received from server: " + echoedMessage);*/
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
