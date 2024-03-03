package client;

import message.*;
import models.MessageWrapper;
import models.Metadata;
import models.MonitorClient;
import utilities.CustomSerializationUtil;
import utilities.PropertyUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Properties;

public class ClientMain {
    //Init Client Param
    public static final Properties properties = PropertyUtil.getProperty();

    public static void main(String[] args) {
        // init handlers
        ClientCommandHandler clientCommandHandler = ClientCommandHandler.getInstance();
        ClientCacheHandler clientCacheHandler = ClientCacheHandler.getInstance();

        int requestID = 1;
        System.out.println("Type help to see all available commands");
        try {
            DatagramSocket socket = new DatagramSocket(4600);
            InetAddress serverAddress = InetAddress.getByName(properties.getProperty("SERVER_HOSTNAME"));
            int serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            // Start a separate thread for receiving messages from the server
            Thread receiverThread = new Thread(() -> {
                try {
                    while (true) {
                        byte[] receiveData = new byte[1024]; // Adjust buffer size as needed
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        socket.receive(receivePacket);
                        // Process and print response
                        ByteBuffer buffer = ByteBuffer.wrap(receivePacket.getData());
                        ReplyMessage reply = new ReplyMessage();

                            CustomSerializationUtil.unmarshal(reply, buffer);
                            System.out.println("Received from server: "
                                    + "Command:" + reply.getCommandType()
                                    + ", StatusCode:" + reply.getStatusCode()
                                    + ", StatusMessage:" + reply.getStatusMessage()
                                    + "\nContent:" + reply.getContent());

                            if (Objects.equals(reply.getCommandType(), "READ")) {
                                // read the content, store in cache if cannot find
                                String[] data = reply.getContent().split(","); // should return fileContent,offset,length
                                clientCacheHandler.cacheIfAbsent(reply.getFilePath(), Long.parseLong(data[1]), new ClientCacheData(Integer.parseInt(data[2]), data[0]));
                            }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            receiverThread.start(); // Start the receiver thread

            while (true) {
                MessageWrapper messageWrapper = new MessageWrapper();
                String input = reader.readLine();

                if (Objects.equals(input, "help")) {
                    clientCommandHandler.HelpCommand();
                    continue;
                }

                // send byte to server
                try {
                    messageWrapper = clientCommandHandler.ConvertCommandToObject(requestID++, InetAddress.getLocalHost().getHostAddress(), input);
                    if (messageWrapper == null) continue;
                }
                catch (IllegalArgumentException e) {
                    System.out.println("ERROR: " + e.getMessage() + ", see help for more info.");
                    continue;
                }

                byte[] sendBuffer = CustomSerializationUtil.marshal(messageWrapper);

                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);
                socket.send(sendPacket);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
