package server.handler;

import message.BaseMessage;
import message.MetaMessage;
import message.MonitorMessage;
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
        System.out.println(messageType);

        if(messageType.equals(baseMessageClassName)){
            BaseMessage baseMessage = new BaseMessage();
            CustomSerializationUtil.unmarshal(baseMessage, buffer);
            processBaseMessage(baseMessage);
            reply = CustomSerializationUtil.marshal(baseMessage);
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
            reply = CustomSerializationUtil.marshal(monitorMessage);
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
