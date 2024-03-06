package message;

/*
 * ReplyMessage contains all the information needed for server reply to Client's request
 */
public class ReplyMessage extends BaseMessage{
    //Status Code follows HTTP status codes to represent the service invocation status. e.g. 200(Success), 400(Bad request)...
    private int statusCode;
    //Give a proper representation of the status code
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

    @Override
    public String toString() {
        return "\t\t" + super.toString() + "Status Code: " + this.statusCode +
                ", Status Message: " + this.statusMessage
                + "\n\t\tContent: " + super.getContent();
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
