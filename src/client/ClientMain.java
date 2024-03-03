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

    public static void HelpCommand() {
        System.out.println("COMMANDS");
        System.out.println("============");
        System.out.println("help");
        System.out.println("monitor <filepath> <duration> -- monitors a certain file for updates");
        System.out.println("read <filepath> <offset> <noOfBytesToRead> -- reads a section of a file" );
        System.out.println("update <filepath> -- updates a section of a file offset by bytes");
        System.out.println("copy <filepath> -- copies a file to a new file");
        System.out.println("============");
    }

    public static MessageWrapper ConvertCommandToObject(int currRequestID, String ipAddress, String input) {
        MessageWrapper messageWrapper = new MessageWrapper();

        String[] inputs = input.split(" ");
        String command = inputs[0];
        String requestID = ipAddress + "/" + currRequestID; // once server side ID change to string, change to this
        switch (command) {
            case "monitor":
                if (inputs.length < 3) {
                    throw new IllegalArgumentException("monitor requires 2 arguments. You entered " + (inputs.length - 1));
                }
                MonitorClient monitorClient = new MonitorClient("localhost", 4600, Integer.parseInt(inputs[2]));
                MonitorMessage monitorMessage = new MonitorMessage(currRequestID, "REGISTER", inputs[1],
                        "", monitorClient);

                messageWrapper.setMessage(monitorMessage);
                messageWrapper.setMessageType(monitorMessage.getClass().getSimpleName());
                break;
            case "read":
                if (inputs.length < 4) {
                    throw new IllegalArgumentException("read requires 3 arguments. You entered " + (inputs.length - 1));
                }
                RequestMessage requestMessage = new RequestMessage(currRequestID, "READ", inputs[1],
                        "", Long.parseLong(inputs[2]), Integer.parseInt(inputs[3]));
                messageWrapper.setMessage(requestMessage);
                messageWrapper.setMessageType(requestMessage.getClass().getSimpleName());
                break;
            default:
                BaseMessage baseMessage = new BaseMessage(currRequestID, "BLANK", "", input);
                messageWrapper.setMessage(baseMessage);
                messageWrapper.setMessageType(baseMessage.getClass().getSimpleName());
        }
        return messageWrapper;
    }



    public static void main(String[] args) {
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
                    HelpCommand();
                    continue;
                }

                // send byte to server
                try {
                    messageWrapper = ConvertCommandToObject(requestID++, InetAddress.getLocalHost().getHostAddress(), input);
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
