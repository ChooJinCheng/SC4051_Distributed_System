package message;

public class ReplyMessage extends BaseMessage{
    private int statusCode;
    private String statusMessage;

    public ReplyMessage() {

    }

    public ReplyMessage(String commandType, String filePath, String content) {
        super(commandType, filePath, content);
        this.statusCode = 100;
        this.statusMessage = "INIT";
    }
    public ReplyMessage(String commandType, String filePath, String content, int statusCode, String statusMessage) {
        super(commandType, filePath, content);
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
