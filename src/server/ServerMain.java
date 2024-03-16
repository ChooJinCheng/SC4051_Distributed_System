package server;

import server.handler.ServerHandler;
import utilities.PropertyUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Properties;

/*
* The Server's main execution. It handles all sockets operations such as receiving, sending, construction of datagrampackets
* All processing of received messages and replies will be passed ot ServerHandler to be processed
*/
public class ServerMain {
    public static void main(String[] args) {
        //Init Server Parameters. Server's configuration resides in config.properties
        Properties properties = PropertyUtil.getProperty();
        int port = Integer.parseInt(properties.getProperty("SERVER_PORT"));
        //Indicates the probability for simulation of message loss
        //double requestLossProbability = 0.2, replyLossProbability = 0.5;
        double requestLossProbability = 0.0, replyLossProbability = 0.0;

        try (DatagramSocket socket = new DatagramSocket(port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            //Choice of starting the server as AtLeastOnce or AtMostOnce invocation semantic
            boolean atLeastOnce = handleInvocationInput(reader);
            String semanticMode = atLeastOnce ? "AtLeastOnce" : "AtMostOnce";

            ServerHandler serverHandler = new ServerHandler(atLeastOnce);
            System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "]" + " Server is on invocation semantic " + semanticMode);
            System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "]" + " Server is listening on port " + port);

            while (true) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                byte[] requestData = receivePacket.getData();

                //Simulate packet loss of request message
                if(Math.random() < requestLossProbability){
                    System.out.println("[" + new Timestamp(System.currentTimeMillis())  + "]"  + " Packet loss of request message occurred");
                    continue;
                }

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                //Process the message received and generate a reply
                byte[] sendBuffer = serverHandler.processRequestAndGetReply(requestData);
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);

                //Simulate packet loss of reply message
                if(Math.random() < replyLossProbability){
                    System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "]"   +" Packet loss of reply message occurred");
                    continue;
                }

                System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "]"   + " Reply message sent to IP: " + clientAddress + ", Port: " + clientPort);
                socket.send(sendPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Reading in User's input for invocation semantic choice
     */
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
