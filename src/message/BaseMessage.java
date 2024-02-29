package message;

public class BaseMessage {
    private int requestID;
    private String commandType;
    private String filePath;
    private String content;
    private int statusCode;
    private String errorMessage;

    // Constructors, getters, setters, and other methods
    public BaseMessage() {

    }
    public BaseMessage(int requestID, String commandType, String filePath, String content, int statusCode, String errorMessage) {
        this.requestID = requestID;
        this.commandType = commandType;
        this.filePath = filePath;
        this.content = content;
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
