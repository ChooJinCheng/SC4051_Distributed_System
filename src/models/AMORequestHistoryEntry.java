package models;

public class AMORequestHistoryEntry {
    private final byte[] replyMessage;
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
