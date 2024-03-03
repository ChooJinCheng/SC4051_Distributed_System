package client;

import message.BaseMessage;
import message.MetaMessage;
import message.MonitorMessage;
import models.MessageWrapper;
import models.Metadata;
import models.MonitorClient;
import utilities.CustomSerializationUtil;
import utilities.PropertyUtil;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.Properties;

public class ClientMain {
    //Init Client Param
    public static final Properties properties = PropertyUtil.getProperty();
    public static void main(String[] args) {
        /*Metadata metadata = new Metadata();
        metadata.setFileLocation("fileLoc");
        metadata.setModifiedDate("modDate");

        MetaMessage metaMessage = new MetaMessage();
        metaMessage.setCommandType("READ");
        metaMessage.setFilePath("C://blah");
        metaMessage.setStatusCode(200);
        metaMessage.setErrorMessage("test");
        metaMessage.setContent("");
        metaMessage.setMetadata(metadata);

        try (DatagramSocket socket = new DatagramSocket(4600)) {

            MessageWrapper messageWrapper = new MessageWrapper(metaMessage.getClass().getSimpleName(), metaMessage);
            byte[] sendBuffer = CustomSerializationUtil.marshal(messageWrapper);
            InetAddress serverAddress = InetAddress.getByName(properties.getProperty("SERVER_HOSTNAME"));
            int serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));

            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);
            socket.send(sendPacket);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            String echoedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Received from server: " + echoedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }*/


        MonitorClient monitorClient = new MonitorClient();
        monitorClient.setClientAddress("localhost");
        monitorClient.setClientPort(4600);
        monitorClient.setMonitorInterval(40);

        MonitorMessage monitorMessage = new MonitorMessage();
        monitorMessage.setMonitorClient(monitorClient);
        monitorMessage.setContent("");
        monitorMessage.setCommandType("REGISTER");
        monitorMessage.setFilePath("fileA\\test.txt");
        monitorMessage.setRequestID(1);
        try (DatagramSocket socket1 = new DatagramSocket(4600); DatagramSocket socket2 = new DatagramSocket(4601)){
            System.out.println("Client1 is listening on port " + 4600);
            System.out.println("Client2 is listening on port " + 4601);

            MessageWrapper messageWrapper = new MessageWrapper(monitorMessage.getClass().getSimpleName(), monitorMessage);
            byte[] sendBuffer = CustomSerializationUtil.marshal(messageWrapper);
            InetAddress serverAddress = InetAddress.getByName(properties.getProperty("SERVER_HOSTNAME"));
            int serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));

            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);
            socket1.send(sendPacket);
            while(true){
                byte[] receiveBuffer1 = new byte[1024];
                DatagramPacket receivePacket1 = new DatagramPacket(receiveBuffer1, receiveBuffer1.length);
                socket1.receive(receivePacket1);


                ByteBuffer buffer = ByteBuffer.wrap(receivePacket1.getData());
                String messageType = CustomSerializationUtil.unmarshalMessageType(buffer);
                if(messageType.equals(MonitorMessage.class.getSimpleName())){
                    MonitorMessage newMonitorMessage = new MonitorMessage();
                    CustomSerializationUtil.unmarshal(newMonitorMessage, buffer);
                    System.out.println("Received from socket1: "
                            + "\nMessageType:" + messageType
                            + "\nCommand:" + newMonitorMessage.getCommandType()
                            + "\nContent:" + newMonitorMessage.getContent());
                }
                else if(messageType.equals(BaseMessage.class.getSimpleName())){
                    BaseMessage newBaseMessage = new BaseMessage();
                    CustomSerializationUtil.unmarshal(newBaseMessage, buffer);
                    System.out.println("Received from socket1: "
                            + "\nMessageType:" + messageType
                            + "\nCommand:" + newBaseMessage.getCommandType()
                            + "\nContent:" + newBaseMessage.getContent());
                }


                /*byte[] receiveBuffer2 = new byte[1024];
                DatagramPacket receivePacket2 = new DatagramPacket(receiveBuffer2, receiveBuffer2.length);
                socket2.receive(receivePacket2);

                BaseMessage baseMessage2 = new BaseMessage();
                CustomSerializationUtil.unmarshal(baseMessage2, receivePacket2.getData());
                System.out.println("Received from socket2: "
                        + "\nContent:" + baseMessage2.getContent()
                        + "\nID:" + baseMessage2.getRequestID());*/
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
