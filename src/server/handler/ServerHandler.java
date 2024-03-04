package server.handler;

import message.*;
import models.AMORequestHistoryEntry;
import models.MessageWrapper;
import server.services.FileAccessService;
import server.services.FileMonitorService;
import utilities.CustomSerializationUtil;
import utilities.FileDataExtractorUtil;
import utilities.MessageUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerHandler {
    private final boolean atLeastOnce;
    private final Map<String, AMORequestHistoryEntry> processedRequests = new HashMap<>();
    private final String requestMessageClassName = RequestMessage.class.getSimpleName();
    private final String replyMessageClassName = ReplyMessage.class.getSimpleName();
    private final String monitorMessageClassName = MonitorMessage.class.getSimpleName();

    public ServerHandler(boolean atLeastOnce) {
        this.atLeastOnce = atLeastOnce;
        ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredAMOEntries, 1, 10, TimeUnit.MINUTES);
    }

    public byte[] processRequestAndGetReply(byte[] requestData) throws IllegalAccessException {
        byte[] reply = new byte[0];
        ByteBuffer buffer = ByteBuffer.wrap(requestData);
        String messageID = CustomSerializationUtil.unmarshalStringAttribute(buffer);
        String messageType = CustomSerializationUtil.unmarshalStringAttribute(buffer);

        if(!atLeastOnce){
            if(processedRequests.containsKey(messageID)) {
                System.out.println("Function not executed. Duplicate Request for: " + messageID);
                return processedRequests.get(messageID).getReplyMessage();
            }
        }

        if (messageType.equals(requestMessageClassName)) {
            RequestMessage requestMessage = new RequestMessage();
            CustomSerializationUtil.unmarshal(requestMessage, buffer);
            int statusCode = processRequestMessage(requestMessage);

            String replyContent = requestMessage.getContent();
            if (Objects.equals(requestMessage.getCommandType(), "READ") && statusCode == 200)
                replyContent = requestMessage.getContent() + "," + requestMessage.getOffset() + "," + requestMessage.getReadLength() + "," + FileDataExtractorUtil.getLastModifiedTimeInUnix(requestMessage.getFilePath());

            ReplyMessage replyMessage = new ReplyMessage(requestMessage.getCommandType(), requestMessage.getFilePath(), replyContent);
            MessageUtil.setReplyStatusCode(statusCode, replyMessage);

            MessageWrapper messageWrapper = new MessageWrapper(messageID, replyMessageClassName, replyMessage);
            reply = CustomSerializationUtil.marshal(messageWrapper);
        } else if (messageType.equals(monitorMessageClassName)) {
            MonitorMessage monitorMessage = new MonitorMessage();
            CustomSerializationUtil.unmarshal(monitorMessage, buffer);
            int statusCode = processMonitorMessage(monitorMessage);
            ReplyMessage replyMessage = new ReplyMessage(monitorMessage.getCommandType(), monitorMessage.getFilePath(), monitorMessage.getContent());
            MessageUtil.setReplyStatusCode(statusCode, replyMessage);

            MessageWrapper messageWrapper = new MessageWrapper(messageID, replyMessageClassName, replyMessage);
            reply = CustomSerializationUtil.marshal(messageWrapper);
        } else {
            //ToDo: Throw error/exception
        }

        if(!atLeastOnce){
            processedRequests.putIfAbsent(messageID, new AMORequestHistoryEntry(reply, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)));
        }

        return reply;
    }

    private int processRequestMessage(RequestMessage requestMessage) {
        FileAccessService fileAccessService = FileAccessService.getInstance();
        String commandType = requestMessage.getCommandType();
        int statusCode;

        String filePath = requestMessage.getFilePath();
        long inputOffset = requestMessage.getOffset();
        int inputReadLength = requestMessage.getReadLength();
        String inputContent = requestMessage.getContent();
        String reply = switch (commandType) {
            case "READ" -> fileAccessService.readFileContent(filePath, inputOffset, inputReadLength);
            case "INSERT" -> fileAccessService.insertIntoFile(filePath, inputOffset, inputContent);
            case "GETATTR" -> {
                // can modify this part to return entire file attributes if needed, using FileDataExtractor.getMetadataFromFile
                String lastModifiedDate = Long.toString(FileDataExtractorUtil.getLastModifiedTimeInUnix(filePath));
                yield "200".concat(lastModifiedDate);
            }
            case "COPY" -> fileAccessService.copyFile(filePath);
            default -> "";
        };

        statusCode = MessageUtil.setMessageAndGetStatusCode(reply, requestMessage);

        return statusCode;
    }

    private int processMonitorMessage(MonitorMessage monitorMessage) {
        FileMonitorService fileMonitorService = FileMonitorService.getInstance();
        String commandType = monitorMessage.getCommandType();
        int statusCode = 200;

        if (commandType.equals("REGISTER")) {
            String reply = fileMonitorService.registerClient(monitorMessage.getFilePath(), monitorMessage.getMonitorClient());
            statusCode = MessageUtil.setMessageAndGetStatusCode(reply, monitorMessage);
        }
        return statusCode;
    }

    private void cleanupExpiredAMOEntries() {
        processedRequests.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
