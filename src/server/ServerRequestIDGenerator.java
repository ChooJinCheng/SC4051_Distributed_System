package server;

/*
 * This class advances the id by 1 for each message it has sent.
 * */
public class ServerRequestIDGenerator {
    private static final int MAX_REQUEST_ID = Integer.MAX_VALUE;
    private static int currentRequestId = 1;

    public synchronized static int getNextRequestId() {
        int requestId = currentRequestId;
        if (currentRequestId == MAX_REQUEST_ID) {
            currentRequestId = 1; // Reset to 1 when it reaches the maximum representation
        } else {
            currentRequestId++;
        }
        return requestId;
    }
}
