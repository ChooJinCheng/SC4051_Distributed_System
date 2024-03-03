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

    public static void main(String[] args) {
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
                    messageWrapper = clientCommandHandler.ConvertCommandToObject(requestID++, input);
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
