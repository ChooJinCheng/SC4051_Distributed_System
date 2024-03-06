package client;

import message.MessageWrapper;
import utilities.PropertyUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.Timestamp;
import java.util.Properties;

public class ClientMain {
    //Init Client Param
    public static final Properties properties = PropertyUtil.getProperty();
    public static int requestID = 1;
    public static int freshnessInterval = 60; // default freshness

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

    public static void main(String[] args) {
        // handle freshness Interval argument
        HandleFreshnessInterval(args);

        // init handlers
        ClientCommandHandler clientCommandHandler = ClientCommandHandler.getInstance();
        ClientCacheHandler clientCacheHandler = ClientCacheHandler.getInstance();
        System.out.println("Type help to see all available commands");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            ClientSocketHandler mainSocketHandler = new ClientSocketHandler(properties, "CLIENT_MAIN_PORT");
            ClientSocketHandler monitorSocketHandler = new ClientSocketHandler(properties, "CLIENT_MONITOR_PORT");
            monitorSocketHandler.startReceivingMessages();


            while (true) {
                MessageWrapper messageWrapper = new MessageWrapper();
                String input = reader.readLine();

                // send byte to server
                try {
                    String clientAddress = InetAddress.getLocalHost().getHostAddress();
                    String messageID = clientAddress + "/" + requestID++;
                    messageWrapper = clientCommandHandler.ConvertCommandToObject(messageID, input);
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

                mainSocketHandler.sendAndReceiveTogether(messageWrapper);
            }

        }
        catch (Exception e) {
            System.out.println("ClientMain");
            e.printStackTrace();
        }
    }
}
