package client;

import message.MessageWrapper;

import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;

/*
 * Handles the caching of file contents received from the server
 * Caches based on unique filename, offset and length of the string
 * Caches file content in a hashmap where key = filename and value = ClientCacheData
 */
public class ClientCacheHandler {
    // Creates the hashmap that stores all cached file contents, key is filename
    public HashMap<String, ClientCacheData> cache = new HashMap<>();

    private static ClientCacheHandler clientCacheHandler = null;
    // Create a socket to handle communication with server to check server modified time for a file
    private ClientSocketHandler cacheSocketHandler;

    /*
     * Creates a Singleton instance for the cache handler
     */
    public static synchronized ClientCacheHandler getInstance() {
        if (clientCacheHandler == null)
            clientCacheHandler = new ClientCacheHandler();

        return clientCacheHandler;
    }

    /*
     * Initialises the socket for the cache handler
     */
    public ClientCacheHandler() {
        try {
            cacheSocketHandler = new ClientSocketHandler(ClientMain.properties, "CLIENT_CACHE_PORT");
        } catch (Exception e) {

        }
    }

    /*
     * Check cache to see whether the file contents already exists
     * takes in the filename, offset and length of the file
     * returns the cached file content if it exists, else return null
     */
    public String checkCache(String filePath, long offset, int length) throws Exception {
        // check if filename exists in the cache
        if (cache.containsKey(filePath)) {
            // if exists get the cached file data
            ClientCacheData cacheData = cache.get(filePath);
            // cache hit if request offset and length is in-between the cached data's offset and length
            if (cacheData.offset <= offset && cacheData.length >= length) {
                // check whether cached data is still valid
                if (checkSmelly(filePath, cacheData)) {
                    // if cache data is invalid, return null
                    return null;
                }

                // if data is valid, get remaining time till expire
                long currentFreshness = ClientMain.freshnessInterval - (Instant.now().getEpochSecond() - cacheData.clientLastValidated);
                // get how long the read cache data should be
                // min of the cached data length or the requested length
                // only read up to the cached data length if requested length is longer (prevents over-reading errors)
                int readLength = Math.min(cacheData.content.length(), length);

                // get where the starting offset of the read cache data should be
                // min of the cached data length or the requested offset
                // prevents reading of file contents if offset is not in between the cached data start and end
                int startLength = (int) (offset - cacheData.offset);
                int startLengthValue = Math.min(startLength, cacheData.content.length());

                // return the cached file contents
                return cacheData.content.substring(startLengthValue, readLength) + ", TIME TO EXPIRE (" + currentFreshness + "s)";
            }
        }
        return null;
    }

    /*
     * Check if the cached data is still fresh and whether it is modified in the server
     * Takes in filename and cache data
     * Returns true if modified / not fresh, returns false if still valid
     */
    private boolean checkSmelly(String filePath, ClientCacheData data) {
        // get current time
        long currentTime = Instant.now().getEpochSecond();
        try {
            // check if cached data is still fresh
            if (currentTime - data.clientLastValidated >= ClientMain.freshnessInterval) {
                // if cached data is not fresh, check whether file is modified in server
                // get file's last modified time from server
                long serverModifiedTime = getServerModifiedTimeForFile(filePath);

                // compare cached data last modified time and server's file last modified time
                if (serverModifiedTime == data.serverLastModifiedTimeInUnix) {
                    // if file not modified in server, update clientLastValidated to now, refreshes freshness
                    data.clientLastValidated = Instant.now().getEpochSecond();
                } else {
                    // file modified in server, return true to indicate file is smelly
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // file is not smelly, return false
        return false;
    }

    /*
     * Retrieves the file's last modified time from the server
     * Takes in filename
     * Outputs server's last modified time in unix seconds (long)
     */
    private long getServerModifiedTimeForFile(String filepath) throws Exception {
        String clientAddress = InetAddress.getLocalHost().getHostAddress();
        String messageID = clientAddress + "/" + ClientMain.requestID++;
        // create a messageWrapper object with the commandType getattr
        MessageWrapper messageWrapper = ClientCommandHandler.getInstance().ConvertCommandToObject(messageID, "getattr " + filepath);
        // returns the unix seconds after receiving response from the server
        return Long.parseLong(cacheSocketHandler.sendAndReceiveTogether(messageWrapper));
    }

    /*
     * Caches a new file data if it does not exist in current cache
     * Takes in filename and ClientCacheData
     * Checks if key exists, if offset matches, and if length of new data is greater than current cached data
     * If file is considered new or key does not exist, update cache, else do nothing
     */
    void cacheIfAbsentOrDifferent(String filePath, ClientCacheData data) {
        cache.compute(filePath, (key, currentValue) -> {
            // Check whether if data does not exist in cache for the filename
            // Check whether offset is different or whether length of new data is greater than cached data
            // If any of these conditions are true, means cache does not contain the new data
            // Create a new ClientCacheData and update cache
            if (currentValue == null || currentValue.offset != data.offset || currentValue.length <= data.length) {
                return new ClientCacheData(data.offset, data.length, data.content, data.serverLastModifiedTimeInUnix);
            }
            // If cache already contains the new data, return the current data to leave it unchanged
            return currentValue;
        });
    }

}
