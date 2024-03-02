package message;

public class BaseMessage {
    //IPADDRESS + / + ID
    private int requestID;
    private String commandType;
    private String filePath;
    /*If Client request, command = insert, content = input from user
    If Client request, command = read, content = length of byte read
    If Server reply, command = read, content = file data       */
    private String content;


    // Constructors, getters, setters, and other methods
    public BaseMessage() {

    }
    public BaseMessage(int requestID, String commandType, String filePath, String content) {
        this.requestID = requestID;
        this.commandType = commandType;
        this.filePath = filePath;
        this.content = content;
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
}
