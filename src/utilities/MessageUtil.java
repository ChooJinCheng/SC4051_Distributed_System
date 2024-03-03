package utilities;

import message.BaseMessage;
import message.ReplyMessage;
import message.RequestMessage;

public class MessageUtil {

    public static int setMessageAndGetStatusCode(String reply, BaseMessage requestMessage){
        int statusCode = Integer.parseInt(reply.substring(0,3));
        requestMessage.setContent(reply.substring(3));
        requestMessage.setCommandType("ACK");

        return statusCode;
    }
    public static void setReplyStatusCode(int statusCode, ReplyMessage replyMessage){
        switch (statusCode){
            case 400 -> {
                replyMessage.setStatusCode(400);
                replyMessage.setStatusMessage("Bad Request");
            }
            case 404 -> {
                replyMessage.setStatusCode(404);
                replyMessage.setStatusMessage("Not Found");
            }
            case 500 -> {
                replyMessage.setStatusCode(500);
                replyMessage.setStatusMessage("Internal Server Error");
            }
        }
    }
}
