package message;

/*
* BaseMessage class contains all common fields that all messages will have.
*/
public class BaseMessage {
    //Command Type contains e.g. read, insert, monitor etc.
    private String commandType;
    private String filePath;
    //Contains the main message of the request/reply e.g. file data, error message
    private String content;


    public BaseMessage() {

    }

    public BaseMessage(String commandType, String filePath, String content) {
        this.commandType = commandType;
        this.filePath = filePath;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Command Type: " + commandType +
                ", File Path: " + filePath + ", ";
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
