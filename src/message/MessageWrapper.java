package message;

public class MessageWrapper {
    //IPADDRESS + / + ID
    private String messageID;
    private String messageType;
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
