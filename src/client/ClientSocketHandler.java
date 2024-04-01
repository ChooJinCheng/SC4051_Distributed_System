package client;

import message.ReplyMessage;
import message.MessageWrapper;
import utilities.CustomSerializationUtil;
import utilities.MessageUtil;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/*
 * Handles the socket communication with the server
 */
public class ClientSocketHandler {
    // initialise socket variable
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

    // set how many tries for waiting for response from server
    private int max_timeout;
    // set how long is each try when waiting for response from server
    private int timeout_duration;

    /*
     * Initialises the ClientSocketHandler, creates the socket with the server address and port
     * Set the timeout duration and number of tries
     */
    public ClientSocketHandler(Properties properties, String portName) throws Exception {
        this.socket = new DatagramSocket(Integer.parseInt(properties.getProperty(portName)));
        this.serverAddress = InetAddress.getByName(properties.getProperty("SERVER_HOSTNAME"));
        this.serverPort = Integer.parseInt(properties.getProperty("SERVER_PORT"));

        this.max_timeout = Integer.parseInt(properties.getProperty("MAX_TIMEOUTS"));
        this.timeout_duration = Integer.parseInt(properties.getProperty("TIMEOUT_DURATION"));
    }

    /*
     * Creates a new thread to handle receiving messages from server
     * Used by the monitoring socket to prevent pausing main thread's communication with server
     */
    public void startReceivingMessages() {
        // start a new receiving thread
        Thread receiverThread = new Thread(() -> {
            try {
                // listen for server response
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

                    MessageUtil.printReplyMessage(messageID, reply.getCommandType(), reply.getStatusCode(), reply.getStatusMessage(), reply.getContent());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        receiverThread.start();
    }

    /*
     * Takes in a messageWrapper object and converts to byte
     * Sends packet to server
     */
    public void sendMessage(MessageWrapper message) throws Exception {
        byte[] sendBuffer = CustomSerializationUtil.marshal(message);
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, serverPort);
        socket.send(sendPacket);
    }

    /*
     * Handles sending of message to server and response from server
     */
    public String sendAndReceiveTogether(MessageWrapper message) throws Exception {
        try {
            // create a new current timeout tries tracker
            int timeoutTimes = 0;
            // initialise response packet buffer
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            // check if timeouts tries reached limit
            while (timeoutTimes < max_timeout) {

                // send message to server
                sendMessage(message);

                // set timeout duration for receiving response
                socket.setSoTimeout(timeout_duration);

                try {
                    // wait for response from server
                    socket.receive(receivePacket);
                    // exit loop if received a response
                    break;
                }
                // if timeout, increment current timeout tries, and continue loop
                catch (SocketTimeoutException e) {
                    timeoutTimes++;
                    System.err.println("[" + new Timestamp(System.currentTimeMillis()) + "]"  + " TIMEOUT: DID NOT RECEIVE MESSAGE: " + timeoutTimes + "/" + max_timeout + ", trying to send and receive the message again..");
                    if (timeoutTimes == max_timeout) {
                        throw new SocketTimeoutException("[" + new Timestamp(System.currentTimeMillis()) + "]"  + " ERROR: Time out trying to receive message, try sending the command again.");
                    }
                }
            }

            // get response from server and unmarshal the response
            ByteBuffer buffer = ByteBuffer.wrap(receivePacket.getData());
            String messageID = CustomSerializationUtil.unmarshalStringAttribute(buffer);
            String messageType = CustomSerializationUtil.unmarshalStringAttribute(buffer);
            ReplyMessage reply = new ReplyMessage();
            String replyContent = "";
            CustomSerializationUtil.unmarshal(reply, buffer);

            // if response is successful
            if( reply.getStatusCode() == 200){
                // special handling of response for READ and GETATTR
                switch (reply.getCommandType()) {
                    // handle read response
                    case "READ":
                        // should return fileContent,offset,length, serverLastModifiedInUnix
                        String[] data = reply.getContent().split(",");
                        // updates cache with the file contents if does not exist in cache
                        ClientCacheHandler.getInstance().cacheIfAbsentOrDifferent(reply.getFilePath(),
                                new ClientCacheData(Long.parseLong(data[1]), Integer.parseInt(data[2]), data[0], Long.parseLong(data[3])));
                        replyContent = data[0];
                        break;
                    // handle getattr response
                    case "GETATTR":
                        return reply.getContent();

                }
            }

            // handles the other responses type that are not READ or GETATTR, generic responses
            replyContent = replyContent.isEmpty() ? reply.getContent() : replyContent;
            // print out response message
            MessageUtil.printReplyMessage(messageID, reply.getCommandType(), reply.getStatusCode(), reply.getStatusMessage(), replyContent);
            // return reply contents
            return reply.getContent();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        // return null if any errors
        return null;
    }

}
