package client;

import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class ClientCacheData {

    long offset;
    int length;
    String content;
    long serverLastModifiedTimeInUnix; // in unix seconds

    long clientLastValidated; // in unix seconds

    public ClientCacheData(long offset, int length, String content, long serverLastModified) {
        this.offset = offset;
        this.length = length;
        this.content = content;
        this.serverLastModifiedTimeInUnix = serverLastModified;
        this.clientLastValidated = Instant.now().getEpochSecond();
    }
}
