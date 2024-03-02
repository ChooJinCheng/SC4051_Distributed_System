package models;

import message.BaseMessage;

public class MessageWrapper {
    private String messageType;
    private Object message;

    public MessageWrapper(){}

    public MessageWrapper(String messageType, Object message) {
        this.messageType = messageType;
        this.message = message;
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
