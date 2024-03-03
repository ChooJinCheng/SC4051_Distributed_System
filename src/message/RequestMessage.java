package message;

public class RequestMessage extends BaseMessage {
    private long offset;
    private int readLength;

    public RequestMessage(int requestID, String commandType, String filePath, String content, long offset, int readLength) {
        super(requestID, commandType, filePath, content);
        this.offset = offset;
        this.readLength = readLength;
    }
}
