package server.handler;

import message.*;
import models.AMORequestHistoryEntry;
import message.MessageWrapper;
import server.services.FileAccessService;
import server.services.FileMonitorService;
import utilities.CustomSerializationUtil;
import utilities.FileDataExtractorUtil;
import utilities.MessageUtil;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * The core for processing all messages received by the server such as marshalling and unmarshalling of messages according to their object types
 * The handler can either execute in AtLeastOnce or AtMostOnce invocation semantic
 * Each message object will then be processed according to the client's invoked service and execute the service as requested
 * Once executions are done, a reply will be generated to return or inform client on their request
 */
public class ServerHandler {
    private final boolean atLeastOnce;
    //This hashmap maps messageID to a entry object that is used to track all processed request (used when AtMostOnce is invoked)
    private final Map<String, AMORequestHistoryEntry> processedRequests = new HashMap<>();
    /*
     * Obtain the class names of existing message objects to identify incoming message
     * This enables future scalability of other services that maybe provided without affecting current ones
     */
    private final String requestMessageClassName = RequestMessage.class.getSimpleName();
    private final String replyMessageClassName = ReplyMessage.class.getSimpleName();
    private final String monitorMessageClassName = MonitorMessage.class.getSimpleName();

    public ServerHandler(boolean atLeastOnce) {
        this.atLeastOnce = atLeastOnce;
        ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
        //Thread a scheduler to maintain the processedRequest Hashmap and remove all expired entries at a interval. Interval of execution can be instantiated here.
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredAMOEntries, 1, 10, TimeUnit.MINUTES);
    }

    /*
     * Process the received bytes according to the message types and generate a reply
     */
    public byte[] processRequestAndGetReply(byte[] requestData) throws IllegalAccessException {
        byte[] reply = new byte[0];
        ByteBuffer buffer = ByteBuffer.wrap(requestData);
        //Unmarshall the bytes to obtain both the messageID and messageType for further processing
        String messageID = CustomSerializationUtil.unmarshalStringAttribute(buffer);
        String messageType = CustomSerializationUtil.unmarshalStringAttribute(buffer);

        //If AtMostOnce is invoked, check for duplicate request. Return the recorded reply message for the duplicated request
        if(!atLeastOnce){
            if(processedRequests.containsKey(messageID)) {
                System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "]"  + " Duplicate Request for: " + messageID
                        + "\nFunction not executed. Reply message retrieved from history.");
                return processedRequests.get(messageID).getReplyMessage();
            }
        }

        //Based on the messageType indicated, message will then be unmarshalled accordingly to their respective message object class and passed to process method for service execution
        if (messageType.equals(requestMessageClassName)) {
            RequestMessage requestMessage = new RequestMessage();
            CustomSerializationUtil.unmarshal(requestMessage, buffer);
            MessageUtil.printRequestMessage(messageID, messageType, requestMessage);

            //Unmarshalled message will be passed to process method for processing and obtain the status code of the execution
            int statusCode = processRequestMessage(requestMessage);
            //Metadata will be attached to the message for caching purpose if it is a read service invocation
            String replyContent = requestMessage.getContent();
            if (Objects.equals(requestMessage.getCommandType(), "READ") && statusCode == 200)
                replyContent = requestMessage.getContent() + "," + requestMessage.getOffset() + "," + requestMessage.getReadLength() + "," + FileDataExtractorUtil.getLastModifiedTimeInUnix(requestMessage.getFilePath());

            //Creation of reply message
            ReplyMessage replyMessage = new ReplyMessage(requestMessage.getCommandType(), requestMessage.getFilePath(), replyContent);
            //Update of status code in reply message according to returned statusCode from processMessage method
            MessageUtil.setReplyStatusCode(statusCode, replyMessage);

            //Message is then wrapped in MessageWrapper class before marshalling
            MessageWrapper messageWrapper = new MessageWrapper(messageID, replyMessageClassName, replyMessage);
            reply = CustomSerializationUtil.marshal(messageWrapper);
        } else if (messageType.equals(monitorMessageClassName)) { //Similarly for monitorMessage class
            MonitorMessage monitorMessage = new MonitorMessage();
            CustomSerializationUtil.unmarshal(monitorMessage, buffer);
            MessageUtil.printMonitorMessage(messageID, messageType, monitorMessage);

            int statusCode = processMonitorMessage(monitorMessage);
            ReplyMessage replyMessage = new ReplyMessage(monitorMessage.getCommandType(), monitorMessage.getFilePath(), monitorMessage.getContent());
            MessageUtil.setReplyStatusCode(statusCode, replyMessage);

            MessageWrapper messageWrapper = new MessageWrapper(messageID, replyMessageClassName, replyMessage);
            reply = CustomSerializationUtil.marshal(messageWrapper);
        } else { //Generate a error reply message for any invalid message types provided
            ReplyMessage replyMessage = new ReplyMessage("", "", "Message Type provided does not exist", 400, "Bad Request");
            MessageWrapper messageWrapper = new MessageWrapper(messageID, replyMessageClassName, replyMessage);
            reply = CustomSerializationUtil.marshal(messageWrapper);
        }

        //If AtMostOnce is invoked, records the already processed request message and map the messageID to the reply message. Setting a expiry time for this entry
        if(!atLeastOnce){
            processedRequests.putIfAbsent(messageID, new AMORequestHistoryEntry(reply, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)));
        }

        System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "]"  + " Reply for request: " + messageID + " processed");
        return reply;
    }
    /*
     * Method that process requestMessage that handles read, insert, getMetaData for file access services
     * Obtain the given inputs by the Client and execute the service invoked
     */
    private int processRequestMessage(RequestMessage requestMessage) {
        //Obtaining the file access service class and user's input
        FileAccessService fileAccessService = FileAccessService.getInstance();
        String commandType = requestMessage.getCommandType();
        int statusCode;

        String filePath = requestMessage.getFilePath();
        long inputOffset = requestMessage.getOffset();
        int inputReadLength = requestMessage.getReadLength();
        String inputContent = requestMessage.getContent();
        //Invoke the service requested and return a reply
        String reply = switch (commandType) {
            case "READ" -> fileAccessService.readFileContent(filePath, inputOffset, inputReadLength);
            case "INSERT" -> fileAccessService.insertIntoFile(filePath, inputOffset, inputContent);
            case "GETATTR" -> {
                // can modify this part to return entire file attributes if needed, using FileDataExtractor.getMetadataFromFile
                String lastModifiedDate = Long.toString(FileDataExtractorUtil.getLastModifiedTimeInUnix(filePath));
                yield "200".concat(lastModifiedDate);
            }
            case "COPY" -> fileAccessService.copyFile(filePath);
            case "CLEAR" -> fileAccessService.clearFileContent(filePath);
            default -> "400 Error: Command type does not exist.";
        };

        //Set the message content into the given message object and return obtain status code in reply
        statusCode = MessageUtil.setMessageAndGetStatusCode(reply, requestMessage);

        return statusCode;
    }

    /*
     * Method that process monitorMessage that handles monitoring of a filePath
     * Obtain the given inputs by the Client and execute the service invoked
     */
    private int processMonitorMessage(MonitorMessage monitorMessage) {//Similar to the flow above
        FileMonitorService fileMonitorService = FileMonitorService.getInstance();
        String commandType = monitorMessage.getCommandType();
        int statusCode = 200;

        if (commandType.equals("REGISTER")) {
            String reply = fileMonitorService.registerClient(monitorMessage.getFilePath(), monitorMessage.getMonitorClient());
            statusCode = MessageUtil.setMessageAndGetStatusCode(reply, monitorMessage);
        }
        return statusCode;
    }

    /*
     * Method that checks all the entries in the processedRequest hashmap and removed any entries that have expired from the hashmap
     */
    private void cleanupExpiredAMOEntries() {
        processedRequests.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
