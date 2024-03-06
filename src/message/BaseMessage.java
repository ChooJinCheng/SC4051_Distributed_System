package message;

public class BaseMessage {
    private String commandType;
    private String filePath;
    /*If Client request, command = insert, content = input from user
    If Server reply, command = read, content = file data       */
    private String content;


    // Constructors, getters, setters, and other methods
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
