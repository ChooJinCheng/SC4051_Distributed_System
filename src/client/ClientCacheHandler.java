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
    public HashMap<String, HashMap<Long, ClientCacheData>> cache = new HashMap<>();

    private static ClientCacheHandler clientCacheHandler = null;
    private ClientSocketHandler cacheSocketHandler;
    private int freshnessInterval = 5; // in seconds, later configure via java main argument

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
        if (cache.containsKey(filePath) && cache.get(filePath).containsKey(offset)) {
            ClientCacheData cacheData = cache.get(filePath).get(offset);
            if (cacheData.length == length) {
                // cache hit
                // check whether smelly
                if (checkSmelly(filePath, cacheData)) {
                    System.out.println("SMELLY");
                    return null;
                }

                return cacheData.content;
            }
        }
        return null;
    }

    private boolean checkSmelly(String filePath, ClientCacheData data) {
        long currentTime = Instant.now().getEpochSecond();
        try {
            if (currentTime - data.clientLastValidated > freshnessInterval) {
                // invalid entry
                long serverModifiedTime = getServerModifiedTimeForFile(filePath);
                if (serverModifiedTime == data.serverLastModifiedTimeInUnix) {
                    // if file not modified in server, update clientLastValidated to now
                    data.clientLastValidated = Instant.now().getEpochSecond();
                }
                else {
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

    void cacheIfAbsent(String filePath, long offset, ClientCacheData segment) {
        cache.computeIfAbsent(filePath, k -> new HashMap<>()).put(offset, segment);
    }

}
