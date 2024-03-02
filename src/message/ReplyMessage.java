package message;

public class ReplyMessage extends BaseMessage{
    private int statusCode;
    private String errorMessage;

    public ReplyMessage(int requestID, String commandType, String filePath, String content, int statusCode, String errorMessage) {
        super(requestID, commandType, filePath, content);
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }
}
