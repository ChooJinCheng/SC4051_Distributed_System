package client;

import message.MessageWrapper;
import utilities.PropertyUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.Timestamp;
import java.util.Properties;

/*
 * The main function for the client side of the application.
 * Initialises all the different handlers necessary and handles the freshness interval argument needed to start the client
 * Handles the communication with the server side.
 */
public class ClientMain {
    // Initialise properties used in client side
    public static final Properties properties = PropertyUtil.getProperty();
    // Start requestID sent to server as 1
    public static int requestID = 1;
    // Default freshness interval
    public static int freshnessInterval = 60; // default freshness

    /*
    * Handles the arguments input by user when starting the application
    * Currently only handles freshness interval
    * Will error if no arguments are given, or argument given is not an number
    */

    private static void HandleFreshnessInterval(String[] args) {
        try {
            if (args.length == 0) {
                throw new IllegalArgumentException("ERROR: No arguments found (You need to have one integer for freshnessInterval)");
            }
            freshnessInterval = Integer.parseInt(args[0]);
            System.out.println("Successfully set freshnessInterval to " + freshnessInterval);
        }
        catch (NumberFormatException e) {
            System.err.println("ERROR: The first argument must be a integer");
            System.exit(1);
        }
        catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /*
     * Main function of the client. Initialises the singleton handlers necessary in the application
     * Has a while true loop to continuously handle inputs and response with the server
     */
    public static void main(String[] args) {
        // handle freshness Interval argument
        HandleFreshnessInterval(args);

        // init handlers
        ClientCommandHandler clientCommandHandler = ClientCommandHandler.getInstance();
        ClientCacheHandler clientCacheHandler = ClientCacheHandler.getInstance();

        System.out.println("Type help to see all available commands");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            // init main socket handler for communicating with server
            ClientSocketHandler mainSocketHandler = new ClientSocketHandler(properties, "CLIENT_MAIN_PORT");
            // init monitor socket handler for monitoring file changes in the server
            ClientSocketHandler monitorSocketHandler = new ClientSocketHandler(properties, "CLIENT_MONITOR_PORT");
            // monitor socket handler starts listening
            monitorSocketHandler.startReceivingMessages();


            while (true) {
                MessageWrapper messageWrapper;
                // take in input from the user
                String input = reader.readLine();

                // send byte to server
                try {
                    String clientAddress = InetAddress.getLocalHost().getHostAddress();
                    // increment requestID and combine with clientAddress to form messageID
                    String messageID = clientAddress + "/" + requestID++;
                    // Create a new message object based on the command input from user
                    messageWrapper = clientCommandHandler.ConvertCommandToObject(messageID, input);
                    // if no message created, means no need to send to server - is client side command or error, skip
                    if (messageWrapper == null) continue;
                }
                catch (NumberFormatException e) {
                    System.err.println("ERROR: One of the arguments entered is not a integer, see help for more info");
                    continue;
                }
                catch (Exception e) {
                    System.err.println("ERROR: " + e.getMessage() + ", see help for more info.");
                    continue;
                }

                // socket sends message and await for response
                mainSocketHandler.sendAndReceiveTogether(messageWrapper);
            }

        }
        catch (Exception e) {
            System.out.println("ERROR: ClientMain");
            e.printStackTrace();
        }
    }
}
