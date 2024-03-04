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
            ClientSocketHandler clientSocketHandler = new ClientSocketHandler(properties, 4600);
            clientSocketHandler.startReceivingMessages();

            while (true) {
                MessageWrapper messageWrapper = new MessageWrapper();
                String input = reader.readLine();

                if (Objects.equals(input, "help")) {
                    clientCommandHandler.HelpCommand();
                    continue;
                }

                // send byte to server
                try {
                    String clientAddress = InetAddress.getLocalHost().getHostAddress();
                    String messageID = clientAddress + "/" + requestID++;
                    messageWrapper = clientCommandHandler.ConvertCommandToObject(messageID, input);
                    if (messageWrapper == null) continue;
                }
                catch (IllegalArgumentException e) {
                    System.out.println("ERROR: " + e.getMessage() + ", see help for more info.");
                    continue;
                }

                clientSocketHandler.sendMessage(messageWrapper);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
