package message;

/*
 * MessageWrapper class contains all the information needed to send and receive messages in the Client-Server System
 */
public class MessageWrapper {
    //MessageID is unique through the combination of IPADDRESS of Client/Server and Client/Server side ID generator e.g. 127.0.0.0/1
    private String messageID;
    //MessageType indicates the message class required for unmarshalling of message object on both client and server side
    private String messageType;
    //Message contains the actual message object of the request/reply e.g. ReplyMessage, RequestMessage
    private Object message;

    public MessageWrapper(){}

    public MessageWrapper(String messageID, String messageType, Object message) {
        this.messageID = messageID;
        this.messageType = messageType;
        this.message = message;
    }

    @Override
    public String toString() {
        return "\tMessage ID: " + this.messageID
                + ", Message Type: " + this.messageType
                + ", Message: \n" + this.message.toString();
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }
}
