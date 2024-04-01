package client;

import message.MonitorMessage;
import message.RequestMessage;
import message.MessageWrapper;
import models.MonitorClient;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


/*
 *  Singleton instance that handles all commands input by the user
 *  Will output a message object if need to send a message to the server
 */
public class ClientCommandHandler {
    private static ClientCommandHandler clientCommandHandler = null;

    // Create the Singleton instance if it does not exist
    public static synchronized ClientCommandHandler getInstance()
    {
        if (clientCommandHandler == null)
            clientCommandHandler = new ClientCommandHandler();

        return clientCommandHandler;
    }

    // Output all commands possible as a guide for user to use the program
    public  void HelpCommand() {
        System.out.println("COMMANDS");
        System.out.println("============");
        System.out.println("help");
        System.out.println("monitor <filepath> <duration> -- monitors a certain file for updates");
        System.out.println("read <filepath> <offset> <noOfBytesToRead> -- reads a section of a file" );
        System.out.println("insert <filepath> <offset> <contentToInsert> -- updates a section of a file offset by bytes");
        System.out.println("copy <filepath> -- copies a file to a new file");
        System.out.println("freshness <duration:int> -- update freshness interval in client");
        System.out.println("clear <filepath> -- clear contents inside a file");
        System.out.println("exit -- exit system");
        System.out.println("============");
    }

    /*
     *  Used by ClientMain to handle all user inputs
     *  Converts command to message object if communicating with server
     *  Returns null if is a client-side command
     */
    public MessageWrapper ConvertCommandToObject(String currRequestID, String input) throws Exception {
        MessageWrapper messageWrapper = new MessageWrapper();

        /*
        *  split user inputs into an array for handling
        *  index 0 is always the input command
        */
        String[] inputs = parseArguments(input);
        String command = inputs[0];

        // handle each command respectively
        switch (command) {
            case "help":
                clientCommandHandler.HelpCommand();
                return null;
            case "monitor":
                if (inputs.length < 3) {
                    throw new IllegalArgumentException("monitor requires 2 arguments. You entered " + (inputs.length - 1));
                }
                String clientIp = InetAddress.getLocalHost().getHostAddress();
                // Create a new monitor client and monitor message to be used to listen for messages from the server
                MonitorClient monitorClient = new MonitorClient(clientIp, 4600, Integer.parseInt(inputs[2]));
                MonitorMessage monitorMessage = new MonitorMessage("REGISTER", inputs[1],
                        "", monitorClient);

                // wrap the message in a wrapper object to send to the server
                messageWrapper.setMessageID(currRequestID);
                messageWrapper.setMessage(monitorMessage);
                messageWrapper.setMessageType(monitorMessage.getClass().getSimpleName());
                break;
            case "read":
                if (inputs.length < 4) {
                    throw new IllegalArgumentException("read requires 3 arguments. You entered " + (inputs.length - 1));
                }

                // assign arguments to the respective variable
                String filePath = inputs[1];
                Long offset = Long.parseLong(inputs[2]);
                int length = Integer.parseInt(inputs[3]);

                // check cache for data first
                var message = ClientCacheHandler.getInstance().checkCache(filePath, offset, length);

                // if cache contains message, don't need to communicate with server, return null
                if (message != null) {
                    System.out.println("RETRIEVED FROM CACHE: " + message);
                    return null;
                }

                // if cache does not contain data, create the message object to be sent to the server to read a file
                RequestMessage requestMessage = new RequestMessage("READ", filePath,
                        "", offset, length);
                messageWrapper.setMessageID(currRequestID);
                messageWrapper.setMessage(requestMessage);
                messageWrapper.setMessageType(requestMessage.getClass().getSimpleName());
                break;
            case "insert":
                if (inputs.length < 4) {
                    throw new IllegalArgumentException("insert requires 3 arguments. You entered " + (inputs.length - 1));
                }
                // assign arguments to the respective variable
                filePath = inputs[1];
                offset = Long.parseLong(inputs[2]);
                String content = inputs[3];

                // create a request message with insert commandType to be sent to the server
                requestMessage = new RequestMessage("INSERT", filePath,
                        content, offset, 0);
                messageWrapper.setMessageID(currRequestID);
                messageWrapper.setMessage(requestMessage);
                messageWrapper.setMessageType(requestMessage.getClass().getSimpleName());
                break;
            // getattr is used to get the server modified time for files - cache checking
            case "getattr":
                if (inputs.length < 2) {
                    throw new IllegalArgumentException("getattr requires 1 arguments. You entered " + (inputs.length - 1));
                }

                // create a request message with getAttr commandType to be sent to the server
                requestMessage = new RequestMessage("GETATTR", inputs[1], "");
                messageWrapper.setMessageID(currRequestID);
                messageWrapper.setMessageType(requestMessage.getClass().getSimpleName());
                messageWrapper.setMessage(requestMessage);
                break;
            case "copy":
                // copy requires 2 arguments
                if (inputs.length < 2) {
                    throw new IllegalArgumentException("copy requires 1 arguments. You entered " + (inputs.length - 1));
                }
                filePath = inputs[1];
                // create a request message with getAttr commandType to be sent to the server
                requestMessage = new RequestMessage("COPY", filePath, "");
                messageWrapper.setMessageID(currRequestID);
                messageWrapper.setMessageType(requestMessage.getClass().getSimpleName());
                messageWrapper.setMessage(requestMessage);
                break;
            case "freshness":
                // getattr freshness 2 arguments
                if (inputs.length < 2) {
                    throw new IllegalArgumentException("freshness requires 1 arguments. You entered " + (inputs.length - 1));
                }
                // update the client's freshness interval
                ClientMain.freshnessInterval = Integer.parseInt(inputs[1]);
                System.out.println("Successfully set freshnessInterval to " + ClientMain.freshnessInterval);
                return null;
            case "clear":
                if (inputs.length < 2) {
                    throw new IllegalArgumentException("clear requires 1 arguments. You entered " + (inputs.length - 1));
                }
                // create a request message with clear commandType to be sent to the server
                filePath = inputs[1];
                requestMessage = new RequestMessage("CLEAR", filePath, "");
                messageWrapper.setMessageID(currRequestID);
                messageWrapper.setMessageType(requestMessage.getClass().getSimpleName());
                messageWrapper.setMessage(requestMessage);
                break;
            case "exit":
                // exit the program
                System.exit(0);
            default:
                // invalid command
                throw new IllegalArgumentException("Invalid command");
        }
        // returns null if client-side command, returns messageWrapper object if communicating with server
        return messageWrapper;
    }


    /*
     * This method takes in a string input from the user, and separate it into an array of arguments based on the space delimiter and double quotes , and returns it.
     * It is similar to how command-line arguments are parsed.
     * */
    private String[] parseArguments(String input) {

        //  It starts with creating an empty list argsList to store the arguments and a StringBuilder sb to construct individual arguments.
        //  A boolean flag inQuotes is used to track whether the current character is within quotes.
        List<String> argsList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        // It converts the string into a character array using input.toCharArray()
        // It them iterates over each character in the character array
        for (char ch : input.toCharArray()) {

            //If the current character is a quotation mark ("\""), the method toggles the inQuotes flag
            // but does not add the quotation mark to the StringBuilder (sb).
            if (ch == '\"') {
                inQuotes = !inQuotes;
                //skip adding " to sb
                continue;
            }

            // If the method encounters a space (' ') and inQuotes is false (meaning it's outside of a quoted segment),
            // it then adds the current content of sb as an argument to argsList and reset the sb
            if (ch == ' ' && inQuotes == false) {
                argsList.add(sb.toString());
                //reset sb for next arg
                sb = new StringBuilder();

            // else it continues to append character to sb
            } else {
                sb.append(ch);
            }
        }

        // Add the last argument if any
        // This step ensures that the last argument is not missed, as there might not be a trailing space to trigger its addition.
        if (sb.length() > 0) {
            argsList.add(sb.toString());
        }

        // Convert list into array of args and returns it
        return argsList.toArray(new String[0]);
    }


}
