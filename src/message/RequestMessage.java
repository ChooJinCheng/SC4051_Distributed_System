package message;

public class RequestMessage extends BaseMessage {
    private long offset;
    private int readLength;

    public RequestMessage() {}
    public RequestMessage(String commandType, String filePath, String content) {
        super(commandType, filePath, content);
    }

    public RequestMessage(String commandType, String filePath, String content, long offset, int readLength) {
        super(commandType, filePath, content);
        this.offset = offset;
        this.readLength = readLength;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getReadLength() {
        return readLength;
    }

    public void setReadLength(int readLength) {
        this.readLength = readLength;
    }
}
