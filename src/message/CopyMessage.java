package message;

public class CopyMessage extends BaseMessage{
    public CopyMessage() {
    }

    public CopyMessage(int requestID, String commandType, String filePath, String content) {
        super(requestID, commandType, filePath, content);
    }
}
