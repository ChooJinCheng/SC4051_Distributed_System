package client;

import java.util.HashMap;

public class ClientCacheHandler {
    public HashMap<String, HashMap<Long, ClientCacheData>> cache = new HashMap<>();

    private static ClientCacheHandler clientCacheHandler = null;

    public static synchronized ClientCacheHandler getInstance()
    {
        if (clientCacheHandler == null)
            clientCacheHandler = new ClientCacheHandler();

        return clientCacheHandler;
    }

    public String checkCache(String filePath, long offset, int length) {
        if (cache.containsKey(filePath) && cache.get(filePath).containsKey(offset)) {
            ClientCacheData cacheData = cache.get(filePath).get(offset);
            if (cacheData.length == length) {
                return cacheData.content; // Cache hit
            }
        }
        return null;
    }

    void cacheIfAbsent(String filePath, long offset, ClientCacheData segment) {
        cache.computeIfAbsent(filePath, k -> new HashMap<>()).put(offset, segment);
    }

}
