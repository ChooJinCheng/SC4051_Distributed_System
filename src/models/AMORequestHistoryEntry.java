package models;

/*
* Object class representation for recording processed request. This is used to build history entries table to keep track of duplicate request
* and preventing executions of the request. For AtMostOnce invocation semantics implementations
*/
public class AMORequestHistoryEntry {
    //Store the replymessage of the processed request
    private final byte[] replyMessage;
    //Expiration time of the entry so that table built can be maintained to prevent infinite cache buildup
    private final long expirationTime;

    public AMORequestHistoryEntry(byte[] replyMessage, long expirationTime) {
        this.replyMessage = replyMessage;
        this.expirationTime = expirationTime;
    }

    public byte[] getReplyMessage() {
        return replyMessage;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }
}
