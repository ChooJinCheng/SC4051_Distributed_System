package client;

import message.ReplyMessage;
import models.MessageWrapper;
import utilities.CustomSerializationUtil;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class ClientCacheHandler {
    public HashMap<String, ClientCacheData> cache = new HashMap<>();

    private static ClientCacheHandler clientCacheHandler = null;
    private ClientSocketHandler cacheSocketHandler;
    private int freshnessInterval = 60; // in seconds, later configure via java main argument

    public static synchronized ClientCacheHandler getInstance() {
        if (clientCacheHandler == null)
            clientCacheHandler = new ClientCacheHandler();

        return clientCacheHandler;
    }

    public ClientCacheHandler() {
        try {
            cacheSocketHandler = new ClientSocketHandler(ClientMain.properties, 4601);
        } catch (Exception e) {

        }
    }

    public String checkCache(String filePath, long offset, int length) throws Exception {
        if (cache.containsKey(filePath)) {
            ClientCacheData cacheData = cache.get(filePath);
            // cache hit
            if (cacheData.offset == offset && cacheData.length >= length) {
                // check whether cache still valid
                if (checkSmelly(filePath, cacheData)) {
                    return null;
                }
                long currentFreshness = freshnessInterval - (Instant.now().getEpochSecond() - cacheData.clientLastValidated);
                return cacheData.content.substring(0, length) + ", TIME TO EXPIRE (" + currentFreshness + ")";
            }
        }
        return null;
    }

    private boolean checkSmelly(String filePath, ClientCacheData data) {
        long currentTime = Instant.now().getEpochSecond();
        try {
            if (currentTime - data.clientLastValidated >= freshnessInterval) {
                // invalid entry
                long serverModifiedTime = getServerModifiedTimeForFile(filePath);
                if (serverModifiedTime == data.serverLastModifiedTimeInUnix) {
                    // if file not modified in server, update clientLastValidated to now
                    data.clientLastValidated = Instant.now().getEpochSecond();
                } else {
                    // file modified in server
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private long getServerModifiedTimeForFile(String filepath) throws Exception {
        MessageWrapper messageWrapper = ClientCommandHandler.getInstance().ConvertCommandToObject(ClientMain.requestID, "getattr " + filepath);
        return Long.parseLong(cacheSocketHandler.sendAndReceiveTogether(messageWrapper));
    }

    void cacheIfAbsentOrDifferent(String filePath, ClientCacheData data) {
        cache.compute(filePath, (key, currentValue) -> {
            // If there's no current value or the offset doesnt match or length of new data greater than current value data, replace with new data
            if (currentValue == null || currentValue.offset != data.offset || currentValue.length <= data.length) {
                return new ClientCacheData(data.offset, data.length, data.content, data.serverLastModifiedTimeInUnix);
            }
            // If it matches, return the current data to leave it unchanged
            return currentValue;
        });
    }

}
