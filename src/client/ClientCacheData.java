package client;

import java.time.Instant;

/*
 * Model for the cached data
 * Contains the offset, length and content of the file
 * Contains the serverLastModifiedTime to check whether file is modified in server
 * Contains clientLastValidated to check if cached data is still fresh
 */
public class ClientCacheData {
    // set offset to track starting point of cached data
    long offset;
    // set length to track how long the cached data is from the offset
    int length;
    // set the file contents
    String content;
    // set the current last modified time of the file
    // in unix seconds
    long serverLastModifiedTimeInUnix;
    // set when the file was last validated (freshness)
    // in unix seconds
    long clientLastValidated;

    /*
     * Constructor to set all the values when a new ClientCacheData is created
     */
    public ClientCacheData(long offset, int length, String content, long serverLastModified) {
        this.offset = offset;
        this.length = length;
        this.content = content;
        this.serverLastModifiedTimeInUnix = serverLastModified;
        this.clientLastValidated = Instant.now().getEpochSecond();
    }
}
