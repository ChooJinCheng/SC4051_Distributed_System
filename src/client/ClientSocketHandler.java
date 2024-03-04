package client;

import message.ReplyMessage;
import message.MessageWrapper;
import utilities.CustomSerializationUtil;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Properties;

public class ClientSocketHandler {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

    private int max_timeout;
    private int timeout_duration;

//    private static ClientSocketHandler clientSocketHandler = null;
//
//    public static synchronized ClientSocketHandler getInstance() throws Exception {
//        if (clientSocketHandler == null)
//            clientSocketHandler = new ClientSocketHandler(ClientMain.properties);
//
//        return clientSocketHandler;
//    }

    public InetAddress getServerAddress() {
        return serverAddress;
    }

    public DatagramSocket getSocket() {
        return socket;
    }


    public ClientSocketHandler(Properties properties, String portName) throws Exception {
        this.socket = new DatagramSocket(Integer.parseInt(properties.getProperty(portName)));
        this.serverAddress = InetAddress.getByName(properties.getProperty("SERVER_HOSTNAME"));
        this.serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));

        this.max_timeout = Integer.parseInt(properties.getProperty("MAX_TIMEOUTS"));
        this.timeout_duration = Integer.parseInt(properties.getProperty("TIMEOUT_DURATION"));
    }

    public void startReceivingMessages() {
        Thread receiverThread = new Thread(() -> {
            try {
                while (true) {
                    byte[] receiveData = new byte[1024]; // Adjust buffer size as needed
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    // Process and print response
                    ByteBuffer buffer = ByteBuffer.wrap(receivePacket.getData());
                    String messageID = CustomSerializationUtil.unmarshalStringAttribute(buffer);
                    String messageType = CustomSerializationUtil.unmarshalStringAttribute(buffer);
                    ReplyMessage reply = new ReplyMessage();

                    CustomSerializationUtil.unmarshal(reply, buffer);

                    System.out.println("Received from server: "
                            + "MessageID:" + messageID
                            + ", Command:" + reply.getCommandType()
                            + ", StatusCode:" + reply.getStatusCode()
                            + ", StatusMessage:" + reply.getStatusMessage()
                            + "\nContent:" + reply.getContent());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        receiverThread.start();
    }

    public void sendMessage(MessageWrapper message) throws Exception {
        byte[] sendBuffer = CustomSerializationUtil.marshal(message);
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);
        socket.send(sendPacket);
    }


//    public MessageWrapper receiveMessage() throws Exception {
//        byte[] receiveData = new byte[1024]; // Adjust buffer size as needed
//        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//        socket.receive(receivePacket);
//        // Process and print response
//        ByteBuffer buffer = ByteBuffer.wrap(receivePacket.getData());
//        String messageID = CustomSerializationUtil.unmarshalStringAttribute(buffer);
//        String messageType = CustomSerializationUtil.unmarshalStringAttribute(buffer);
//        ReplyMessage reply = new ReplyMessage();
//
//        CustomSerializationUtil.unmarshal(reply, buffer);
//
//        return new MessageWrapper(messageID, messageType, reply);
//    }

    public String sendAndReceiveTogether(MessageWrapper message) throws Exception {
        try {
            int timeoutTimes = 0;
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            while (timeoutTimes < max_timeout) {

                sendMessage(message);
                socket.setSoTimeout(timeout_duration);

                try {
                    socket.receive(receivePacket);
                    break;
                } catch (SocketTimeoutException e) {
                    timeoutTimes++;
                    System.err.println("TIMEOUT: DID NOT RECEIVE MESSAGE: " + timeoutTimes + "/" + max_timeout + ", trying to send and receive the message again..");
                    if (timeoutTimes == max_timeout) {
                        throw new SocketTimeoutException("ERROR: Time out trying to receive message, try sending the command again.");
                    }
                }
            }

            ByteBuffer buffer = ByteBuffer.wrap(receivePacket.getData());
            String messageID = CustomSerializationUtil.unmarshalStringAttribute(buffer);
            String messageType = CustomSerializationUtil.unmarshalStringAttribute(buffer);
            ReplyMessage reply = new ReplyMessage();

            CustomSerializationUtil.unmarshal(reply, buffer);

            switch (reply.getCommandType()) {
                case "READ":
                    String[] data = reply.getContent().split(","); // should return fileContent,offset,length, serverLastModifiedInUnix
                    ClientCacheHandler.getInstance().cacheIfAbsentOrDifferent(reply.getFilePath(),
                            new ClientCacheData(Long.parseLong(data[1]), Integer.parseInt(data[2]), data[0], Long.parseLong(data[3])));
                    break;
                case "GETATTR":
                    return reply.getContent();

            }

            System.out.println("Received from server: "
                    + "MessageID:" + messageID
                    + ", Command:" + reply.getCommandType()
                    + ", StatusCode:" + reply.getStatusCode()
                    + ", StatusMessage:" + reply.getStatusMessage()
                    + "\nContent:" + reply.getContent());

            return reply.getContent();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            ;
        }
        return null;
    }

}
