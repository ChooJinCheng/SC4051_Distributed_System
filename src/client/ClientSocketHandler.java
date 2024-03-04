package client;

import message.ReplyMessage;
import message.MessageWrapper;
import utilities.CustomSerializationUtil;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Properties;

public class ClientSocketHandler {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

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


    public ClientSocketHandler(Properties properties, int port) throws Exception {
        this.socket = new DatagramSocket(port);
        this.serverAddress = InetAddress.getByName(properties.getProperty("SERVER_HOSTNAME"));
        this.serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));
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
                            + "Command:" + reply.getCommandType()
                            + ", StatusCode:" + reply.getStatusCode()
                            + ", StatusMessage:" + reply.getStatusMessage()
                            + "\nContent:" + reply.getContent());

                    if (Objects.equals(reply.getCommandType(), "READ") && reply.getStatusCode() == 200) {
                        // read the content, store in cache if cannot find
                        String[] data = reply.getContent().split(","); // should return fileContent,offset,length, serverLastModifiedInUnix
                        ClientCacheHandler.getInstance().cacheIfAbsentOrDifferent(reply.getFilePath(),
                                new ClientCacheData(Long.parseLong(data[1]), Integer.parseInt(data[2]), data[0], Long.parseLong(data[3])));
                    }
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

    public String sendAndReceiveTogether(MessageWrapper message) throws Exception {
        try {
            sendMessage(message);

            byte[] receiveData = new byte[1024]; // Adjust buffer size as needed
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            // Process and print response
            ByteBuffer buffer = ByteBuffer.wrap(receivePacket.getData());
            ReplyMessage reply = new ReplyMessage();
            CustomSerializationUtil.unmarshal(reply, buffer);
            return reply.getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
