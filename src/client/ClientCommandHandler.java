package client;

import message.BaseMessage;
import message.CopyMessage;
import message.MonitorMessage;
import message.RequestMessage;
import models.MessageWrapper;
import models.MonitorClient;

import java.lang.module.FindException;
import java.net.InetAddress;
import java.util.Objects;

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
        System.out.println("exit -- exit system");
        System.out.println("============");
    }

    public MessageWrapper ConvertCommandToObject(int currRequestID, String input) throws Exception {
        MessageWrapper messageWrapper = new MessageWrapper();

        String[] inputs = input.split(" ");
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
                MonitorMessage monitorMessage = new MonitorMessage(currRequestID, "REGISTER", inputs[1],
                        "", monitorClient);

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

                RequestMessage requestMessage = new RequestMessage(currRequestID, "READ", filePath,
                        "", offset, length);
                messageWrapper.setMessage(requestMessage);
                messageWrapper.setMessageType(requestMessage.getClass().getSimpleName());
                break;
            case "getattr":
                if (inputs.length < 2) {
                    throw new IllegalArgumentException("getattr requires 1 arguments. You entered " + (inputs.length - 1));
                }
                requestMessage = new RequestMessage(currRequestID, "GETATTR", inputs[1], "");
                messageWrapper.setMessageType(requestMessage.getClass().getSimpleName());
                messageWrapper.setMessage(requestMessage);
                break;
            case "copy":
                if (inputs.length < 2) {
                    throw new IllegalArgumentException("copy requires 1 arguments. You entered " + (inputs.length - 1));
                }
                filePath = inputs[1];
                CopyMessage copyMessage = new CopyMessage(currRequestID,"COPY", filePath, "");
                messageWrapper.setMessage(copyMessage);
                messageWrapper.setMessageType(copyMessage.getClass().getSimpleName());
                break;
            case "exit":
                System.exit(0);
            default:
                BaseMessage baseMessage = new BaseMessage(currRequestID, "BLANK", "", input);
                messageWrapper.setMessage(baseMessage);
                messageWrapper.setMessageType(baseMessage.getClass().getSimpleName());
        }
        return messageWrapper;
    }
}
