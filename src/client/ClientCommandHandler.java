package client;

import message.BaseMessage;
import message.MonitorMessage;
import message.RequestMessage;
import message.MessageWrapper;
import models.MonitorClient;


import java.util.ArrayList;
import java.util.List;

public class ClientCommandHandler {

    private static ClientCommandHandler clientCommandHandler = null;

    public static synchronized ClientCommandHandler getInstance()
    {
        if (clientCommandHandler == null)
            clientCommandHandler = new ClientCommandHandler();

        return clientCommandHandler;
    }

    public  void HelpCommand() {
        System.out.println("COMMANDS");
        System.out.println("============");
        System.out.println("help");
        System.out.println("monitor <filepath> <duration> -- monitors a certain file for updates");
        System.out.println("read <filepath> <offset> <noOfBytesToRead> -- reads a section of a file" );
        System.out.println("update <filepath> -- updates a section of a file offset by bytes");
        System.out.println("copy <filepath> -- copies a file to a new file");
        System.out.println("freshness <duration:int> -- update freshness interval in client");
        System.out.println("exit -- exit system");
        System.out.println("============");
    }

    public MessageWrapper ConvertCommandToObject(String currRequestID, String input) throws Exception {
        MessageWrapper messageWrapper = new MessageWrapper();


//        String[] inputs = input.split(" ");
        String[] inputs = parseArguments(input);
        String command = inputs[0];
//         once server side ID change to string, change to this
//         String ipAddress = InetAddress.getLocalHost().getHostAddress();
//        String requestID = ipAddress + "/" + currRequestID;
        switch (command) {
            case "monitor":
                if (inputs.length < 3) {
                    throw new IllegalArgumentException("monitor requires 2 arguments. You entered " + (inputs.length - 1));
                }
                MonitorClient monitorClient = new MonitorClient("localhost", 4600, Integer.parseInt(inputs[2]));
                MonitorMessage monitorMessage = new MonitorMessage("REGISTER", inputs[1],
                        "", monitorClient);

                messageWrapper.setMessageID(currRequestID);
                messageWrapper.setMessage(monitorMessage);
                messageWrapper.setMessageType(monitorMessage.getClass().getSimpleName());
                break;
            case "read":
                if (inputs.length < 4) {
                    throw new IllegalArgumentException("read requires 3 arguments. You entered " + (inputs.length - 1));
                }
                String filePath = inputs[1];
                Long offset = Long.parseLong(inputs[2]);
                int length = Integer.parseInt(inputs[3]);

                // check cache for data first
                var message = ClientCacheHandler.getInstance().checkCache(filePath, offset, length);

                if (message != null) {
                    System.out.println("RETRIEVED FROM CACHE: " + message); // retrieve message from cache
                    return null; // do not send command to server
                }

                RequestMessage requestMessage = new RequestMessage("READ", filePath,
                        "", offset, length);
                messageWrapper.setMessageID(currRequestID);
                messageWrapper.setMessage(requestMessage);
                messageWrapper.setMessageType(requestMessage.getClass().getSimpleName());
                break;
            case "getattr":
                if (inputs.length < 2) {
                    throw new IllegalArgumentException("getattr requires 1 arguments. You entered " + (inputs.length - 1));
                }
                requestMessage = new RequestMessage("GETATTR", inputs[1], "");
                messageWrapper.setMessageID(currRequestID);
                messageWrapper.setMessageType(requestMessage.getClass().getSimpleName());
                messageWrapper.setMessage(requestMessage);
                break;
            case "copy":
                if (inputs.length < 2) {
                    throw new IllegalArgumentException("copy requires 1 arguments. You entered " + (inputs.length - 1));
                }
                filePath = inputs[1];
                requestMessage = new RequestMessage("COPY", filePath, "");
                messageWrapper.setMessageID(currRequestID);
                messageWrapper.setMessageType(requestMessage.getClass().getSimpleName());
                messageWrapper.setMessage(requestMessage);
                break;
            case "freshness":
                if (inputs.length < 2) {
                    throw new IllegalArgumentException("freshness requires 1 arguments. You entered " + (inputs.length - 1));
                }
                ClientMain.freshnessInterval = Integer.parseInt(inputs[1]);
                System.out.println("Successfully set freshnessInterval to " + ClientMain.freshnessInterval);
                return null;
            case "exit":
                System.exit(0);
            default:
                BaseMessage baseMessage = new BaseMessage("BLANK", "", input);
                messageWrapper.setMessageID(currRequestID);
                messageWrapper.setMessage(baseMessage);
                messageWrapper.setMessageType(baseMessage.getClass().getSimpleName());
        }
        return messageWrapper;
    }

    private String[] parseArguments(String input) {
        List<String> argsList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (char ch : input.toCharArray()) {
            if (ch == '\"') {
                inQuotes = !inQuotes;
                //skip adding " to sb
                continue;
            }

            if (ch == ' ' && inQuotes == false) {
                argsList.add(sb.toString());
                //reset sb for next arg
                sb = new StringBuilder();
            } else {
                sb.append(ch);
            }
        }

        // add the last argument if any
        if (sb.length() > 0) {
            argsList.add(sb.toString());
        }

        // convert list into array of args
        return argsList.toArray(new String[0]);
    }


}
