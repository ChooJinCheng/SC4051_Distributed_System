package server.handler;

import message.BaseMessage;
import message.MetaMessage;
import message.MonitorMessage;
import message.ReplyMessage;
import server.services.FileAccessService;
import server.services.FileMonitorService;
import utilities.CustomSerializationUtil;

import java.nio.ByteBuffer;

public class ServerHandler {
    private boolean atLeastOnce;
    private final String baseMessageClassName = BaseMessage.class.getSimpleName();
    private final String metaMessageClassName = MetaMessage.class.getSimpleName();
    private final String monitorMessageClassName = MonitorMessage.class.getSimpleName();
    public byte[] processRequestAndGetReply(byte[] requestData) throws IllegalAccessException {
        byte[] reply = new byte[0];
        ByteBuffer buffer = ByteBuffer.wrap(requestData);
        String messageType = CustomSerializationUtil.unmarshalMessageType(buffer);

        // sam: this part can refactor to much simpler if reply message is simple
        // or if put monitor client as optional in requestMessage?
        if(messageType.equals(baseMessageClassName)){
            BaseMessage baseMessage = new BaseMessage();
            CustomSerializationUtil.unmarshal(baseMessage, buffer);
            processBaseMessage(baseMessage);
            ReplyMessage replyMessage = new ReplyMessage(baseMessage.getRequestID(), baseMessage.getCommandType(), baseMessage.getFilePath(), baseMessage.getContent(), 200, "SUCCCESS");
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
            processMonitorMessage(monitorMessage);
            ReplyMessage replyMessage = new ReplyMessage(monitorMessage.getRequestID(), monitorMessage.getCommandType(), monitorMessage.getFilePath(), monitorMessage.getContent(), 200, "SUCCCESS");
            reply = CustomSerializationUtil.marshal(replyMessage);
        }else{
            //ToDo: Throw error/exception
        }
        return reply;
    }

    private void processBaseMessage(BaseMessage baseMessage){

    }

    private void processMetaMessage(MetaMessage metaMessage){

    }

    private void processMonitorMessage(MonitorMessage monitorMessage){
        FileMonitorService fileMonitorService = FileMonitorService.getInstance();
        String commandType = monitorMessage.getCommandType();

        if (commandType.equals("REGISTER")) {
            String reply = fileMonitorService.registerClient(monitorMessage.getFilePath(), monitorMessage.getMonitorClient());
            monitorMessage.setContent(reply);
            monitorMessage.setCommandType("ACK");
        }
    }
}
