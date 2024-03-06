package utilities;

import message.*;

import java.sql.Timestamp;
/*
 * This utility class implements the printing/manipulation of the message objects
 * */
public class MessageUtil {
    /*
     * Prints information on the received reply message
     * */
    public static void printReplyMessage(String messageID, String commandType, int statusCode, String statusMessage, String content){
        System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "]"  + " Received from server: "
                + "\n\tMessageID:" + messageID
                + ", Command:" + commandType
                + ", StatusCode:" + statusCode
                + ", StatusMessage:" + statusMessage
                + "\n\tContent:" + content);
    }
    /*
     * Prints information on the received request message
     * */
    public static void printRequestMessage(String messageID, String messageType, RequestMessage requestMessage){
        MessageWrapper messageWrapper = new MessageWrapper(messageID, messageType, requestMessage);
        System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "]"  + " Server Received: \n" + messageWrapper);
    }
    /*
     * Prints information on the received monitor message
     * */
    public static void printMonitorMessage(String messageID, String messageType, MonitorMessage monitorMessage){
        MessageWrapper messageWrapper = new MessageWrapper(messageID, messageType, monitorMessage);
        System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "]"  + " Server Received: \n" + messageWrapper);
    }
    /*
     * This method obtain the status code and the reply content from "reply" value returned by the services
     * */
    public static int setMessageAndGetStatusCode(String reply, BaseMessage requestMessage){
        int statusCode = Integer.parseInt(reply.substring(0,3));
        requestMessage.setContent(reply.substring(3));

        return statusCode;
    }
    /*
     * This method sets replyMessage status fields to appropriate value given a status code
     * */
    public static void setReplyStatusCode(int statusCode, ReplyMessage replyMessage){
        switch (statusCode){
            case 200 -> {
                replyMessage.setStatusCode(200);
                replyMessage.setStatusMessage("Success");
            }
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
