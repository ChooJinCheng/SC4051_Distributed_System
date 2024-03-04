package server.handler;

import message.*;
import models.MessageWrapper;
import server.ServerMain;
import server.services.FileAccessService;
import server.services.FileMonitorService;
import utilities.CustomSerializationUtil;
import utilities.FileDataExtractorUtil;
import utilities.MessageUtil;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ServerHandler {
    private boolean atLeastOnce;
    private Map<String, byte[]> processedRequests = new HashMap<>();
    private final String requestMessageClassName = RequestMessage.class.getSimpleName();
    private final String replyMessageClassName = ReplyMessage.class.getSimpleName();
    private final String monitorMessageClassName = MonitorMessage.class.getSimpleName();

    public ServerHandler(boolean atLeastOnce) {
        this.atLeastOnce = atLeastOnce;
    }

    public byte[] processRequestAndGetReply(byte[] requestData) throws IllegalAccessException {
        byte[] reply = new byte[0];
        ByteBuffer buffer = ByteBuffer.wrap(requestData);
        String messageID = CustomSerializationUtil.unmarshalStringAttribute(buffer);
        String messageType = CustomSerializationUtil.unmarshalStringAttribute(buffer);

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
        return reply;
    }

    private int processRequestMessage(RequestMessage requestMessage) {
        FileAccessService fileAccessService = FileAccessService.getInstance();
        String commandType = requestMessage.getCommandType();
        int statusCode = 200;

        String filePath = requestMessage.getFilePath();
        long inputOffset = requestMessage.getOffset();
        int inputReadLength = requestMessage.getReadLength();
        String inputContent = requestMessage.getContent();
        String reply = "";

        switch (commandType) {
            case "READ":
                reply = fileAccessService.readFileContent(filePath, inputOffset, inputReadLength);
                break;
            case "INSERT":
                reply = fileAccessService.insertIntoFile(filePath, inputOffset, inputContent);
                break;
            case "GETATTR":
                // can modify this part to return entire file attributes if needed, using FileDataExtractor.getMetadataFromFile
                String lastModifiedDate = Long.toString(FileDataExtractorUtil.getLastModifiedTimeInUnix(filePath));
                reply = "200".concat(lastModifiedDate);
                break;
            case "COPY":
                reply = fileAccessService.copyFile(filePath);
                break;
        }

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
}
