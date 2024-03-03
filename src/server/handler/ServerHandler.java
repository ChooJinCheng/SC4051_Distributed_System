package server.handler;

import message.*;
import server.services.FileAccessService;
import server.services.FileMonitorService;
import utilities.CustomSerializationUtil;
import utilities.FileDataExtractorUtil;
import utilities.MessageUtil;

import java.nio.ByteBuffer;
import java.util.Objects;

public class ServerHandler {
    private boolean atLeastOnce;
    private final String requestMessageClassName = RequestMessage.class.getSimpleName();
    private final String metaMessageClassName = MetaMessage.class.getSimpleName();
    private final String monitorMessageClassName = MonitorMessage.class.getSimpleName();

    public byte[] processRequestAndGetReply(byte[] requestData) throws IllegalAccessException {
        byte[] reply = new byte[0];
        ByteBuffer buffer = ByteBuffer.wrap(requestData);
        String messageType = CustomSerializationUtil.unmarshalMessageType(buffer);

        if (messageType.equals(requestMessageClassName)) {
            RequestMessage requestMessage = new RequestMessage();
            CustomSerializationUtil.unmarshal(requestMessage, buffer);
            int statusCode = processRequestMessage(requestMessage);

            String content = Objects.equals(requestMessage.getCommandType(), "READ") ?
                    requestMessage.getContent() + "," + requestMessage.getOffset() + "," + requestMessage.getReadLength() + "," + FileDataExtractorUtil.getLastModifiedTimeInUnix(requestMessage.getFilePath())
                    : requestMessage.getContent();

            ReplyMessage replyMessage = new ReplyMessage(requestMessage.getRequestID(), requestMessage.getCommandType(), requestMessage.getFilePath(), content, 200, "SUCCESS");
            MessageUtil.setReplyStatusCode(statusCode, replyMessage);
            System.out.println(replyMessage.getCommandType());
            reply = CustomSerializationUtil.marshal(replyMessage);
        } else if (messageType.equals(metaMessageClassName)) {
            MetaMessage metaMessage = new MetaMessage();
            CustomSerializationUtil.unmarshal(metaMessage, buffer);
            processMetaMessage(metaMessage);
            reply = CustomSerializationUtil.marshal(metaMessage);
        } else if (messageType.equals(monitorMessageClassName)) {
            MonitorMessage monitorMessage = new MonitorMessage();
            CustomSerializationUtil.unmarshal(monitorMessage, buffer);
            int statusCode = processMonitorMessage(monitorMessage);
            ReplyMessage replyMessage = new ReplyMessage(monitorMessage.getRequestID(), monitorMessage.getCommandType(), monitorMessage.getFilePath(), monitorMessage.getContent(), 200, "SUCCESS");
            MessageUtil.setReplyStatusCode(statusCode, replyMessage);
            reply = CustomSerializationUtil.marshal(replyMessage);
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
                System.out.println(reply);
        }

        statusCode = MessageUtil.setMessageAndGetStatusCode(reply, requestMessage);

        return statusCode;
    }

    private void processMetaMessage(MetaMessage metaMessage) {

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
