package server.handler;

import message.*;
import server.services.FileAccessService;
import server.services.FileMonitorService;
import utilities.CustomSerializationUtil;
import utilities.MessageUtil;

import java.nio.ByteBuffer;

public class ServerHandler {
    private boolean atLeastOnce;
    private final String requestMessageClassName = RequestMessage.class.getSimpleName();
    private final String metaMessageClassName = MetaMessage.class.getSimpleName();
    private final String monitorMessageClassName = MonitorMessage.class.getSimpleName();
    public byte[] processRequestAndGetReply(byte[] requestData) throws IllegalAccessException {
        byte[] reply = new byte[0];
        ByteBuffer buffer = ByteBuffer.wrap(requestData);
        String messageType = CustomSerializationUtil.unmarshalMessageType(buffer);

        // sam: this part can refactor to much simpler if reply message is simple
        // or if put monitor client as optional in requestMessage?
        if(messageType.equals(requestMessageClassName)){
            RequestMessage requestMessage = new RequestMessage();
            CustomSerializationUtil.unmarshal(requestMessage, buffer);
            int statusCode = processRequestMessage(requestMessage);
            ReplyMessage replyMessage = new ReplyMessage(requestMessage.getRequestID(), requestMessage.getCommandType(), requestMessage.getFilePath(), requestMessage.getContent(), 200, "SUCCESS");
            MessageUtil.setReplyStatusCode(statusCode, replyMessage);
            reply = CustomSerializationUtil.marshal(replyMessage);
        }
        else if(messageType.equals(metaMessageClassName)){
            MetaMessage metaMessage = new MetaMessage();
            CustomSerializationUtil.unmarshal(metaMessage, buffer);
            processMetaMessage(metaMessage);
            reply = CustomSerializationUtil.marshal(metaMessage);
        }
        else if(messageType.equals(monitorMessageClassName)){
            MonitorMessage monitorMessage = new MonitorMessage();
            CustomSerializationUtil.unmarshal(monitorMessage, buffer);
            int statusCode = processMonitorMessage(monitorMessage);
            ReplyMessage replyMessage = new ReplyMessage(monitorMessage.getRequestID(), monitorMessage.getCommandType(), monitorMessage.getFilePath(), monitorMessage.getContent(), 200, "SUCCESS");
            MessageUtil.setReplyStatusCode(statusCode, replyMessage);
            reply = CustomSerializationUtil.marshal(replyMessage);
        }else{
            //ToDo: Throw error/exception
        }
        return reply;
    }

    private int processRequestMessage(RequestMessage requestMessage){
        FileAccessService fileAccessService = FileAccessService.getInstance();
        String commandType = requestMessage.getCommandType();
        int statusCode = 200;

        if (commandType.equals("READ")) {
            String filePath = requestMessage.getFilePath();
            long inputOffset = requestMessage.getOffset();
            int inputReadLength = requestMessage.getReadLength();
            String reply = fileAccessService.readFileContent(filePath, inputOffset, inputReadLength);
            statusCode = MessageUtil.setMessageAndGetStatusCode(reply, requestMessage);
        }
        else if(commandType.equals("INSERT")){
            String filePath = requestMessage.getFilePath();
            long inputOffset = requestMessage.getOffset();
            String inputContent = requestMessage.getContent();
            String reply = fileAccessService.insertIntoFile(filePath, inputOffset, inputContent);
            statusCode = MessageUtil.setMessageAndGetStatusCode(reply, requestMessage);
        }
        return statusCode;
    }

    private void processMetaMessage(MetaMessage metaMessage){

    }

    private int processMonitorMessage(MonitorMessage monitorMessage){
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
