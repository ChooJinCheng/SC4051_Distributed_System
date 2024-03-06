package server.services;

import message.ReplyMessage;
import server.ServerRequestIDGenerator;
import message.MessageWrapper;
import models.MonitorClient;
import utilities.CustomSerializationUtil;
import utilities.FileDataExtractorUtil;
import utilities.FilePathUtil;
import utilities.PropertyUtil;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/*
 * File monitor service allow multiple client's to subscribe to any existing file and newly updated file content will be sent to the client's if there is any amendments made in the monitored file
 * Each running thread represents monitoring of a specific file where multiple clients can be assigned to
 * ReentrantLock implemented to guard critical sections that interacts with mutual variable among the threads
 * AtomicBoolean implemented to ensure the change of Boolean for tracking runningThreads can be changed atomically
 * */
public class FileMonitorService {
    //This hashmap maps file path to list of clients to represent client's subscribed to a file
    private static final Map<String, List<MonitorClient>> registeredClients = new HashMap<>();
    //This hashmap maps file path to atomicboolean to represent a file path monitoring thread is active or not
    private static final Map<String, AtomicBoolean> runningThreads = new HashMap<>();
    //This hashmap maps file path to reentrantlock to represent each client can only subscribe to a file path at any point of time. Any concurrent access will have to wait for the lock
    private static final Map<String, ReentrantLock> filePathLocks = new HashMap<>();
    private static FileMonitorService fileMonitorService;

    private FileMonitorService (){}
    //Ensure only one instance of this service is used among multiple clients, preventing multiple threads created by different service object
    public static synchronized FileMonitorService getInstance() {
        if (fileMonitorService == null) {
            fileMonitorService = new FileMonitorService();
        }
        return fileMonitorService;
    }

    /*
     * This methods registers a client to a file path and start a monitoring thread if there isn't an existing one running
     */
    public String registerClient(String inputFilePath, MonitorClient monitorClient) {
        String fullFilePath = FilePathUtil.getFullPathString(inputFilePath);
        //Client have to obtain a lock before any execution of critical section since there are variables that are mutually accessible to the threads
        ReentrantLock lock = getFilePathLock(fullFilePath);
        lock.lock();
        try {
            if(!doesFilePathExist(fullFilePath))
                return "404 Error: File does not exist.";
            //Create a list if file path does not exist and add the client to the list
            registeredClients.computeIfAbsent(fullFilePath, k -> new ArrayList<>()).add(monitorClient);

            //Check if any running threads for the file path, if not start one
            if (!runningThreads.containsKey(fullFilePath) || !runningThreads.get(fullFilePath).get()) {
                startMonitoringThread(fullFilePath);
            }
        } finally {
            lock.unlock();
        }
        return "200 Registration Successful";
    }

    /*
     * This methods executes a monitoring thread for a specific file path
     */
    private void startMonitoringThread(String fullFilePath) {
        ReentrantLock lock = getFilePathLock(fullFilePath);
        lock.lock();
        try {
            if (!runningThreads.containsKey(fullFilePath) || !runningThreads.get(fullFilePath).get()) {
                //update runningthreads to prevent any new threads for monitoring of the same file path
                runningThreads.put(fullFilePath, new AtomicBoolean(true));

                Thread monitoringThread = new Thread(() -> {
                    System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "]" + " Thread for filepath " + fullFilePath + " has started");
                    //record the lastModifiedTime for the filePath
                    FileTime lastModifiedTime = FileDataExtractorUtil.getLastModifiedTime(fullFilePath);
                    while (runningThreads.get(fullFilePath).get()) {
                        try {
                            //Simulate time for monitoring interval
                            Thread.sleep(1000);

                            FileTime currentLastModifiedTime = FileDataExtractorUtil.getLastModifiedTime(fullFilePath);
                            //Compare the modifiedTime at this instance with the previously recorded one, if there is a difference, extract the file content and notify the clients
                            if(lastModifiedTime.compareTo(currentLastModifiedTime) < 0){
                                byte[] fileContent = FileDataExtractorUtil.getContentFromFile(fullFilePath);
                                notifyRegisteredClients(fullFilePath, fileContent);
                                lastModifiedTime = currentLastModifiedTime;
                            }
                            //Update clients intervals and remove any clients if their intervals are over
                            updateClientIntervals(fullFilePath);

                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "]" + " Thread for filepath " + fullFilePath + " has terminated");
                });
                monitoringThread.start();
            }
        } finally {
            lock.unlock();
        }
    }

    /*
     * This methods obtain the list of clients subscribed to this file path and send each of them the updatedcontent obtained from the file
     */
    private void notifyRegisteredClients(String fullFilePath, byte[] updatedContent) {
        //Obtain list of clients
        List<MonitorClient> clients = registeredClients.get(fullFilePath);
        if (clients != null) {
            //For every client, generate a reply message with the updated content, and send the packet to them
            for (MonitorClient client : clients) {
                try (DatagramSocket socket = new DatagramSocket()) {
                    InetAddress clientAddress = InetAddress.getByName(client.getClientAddress());
                    int clientPort = Integer.parseInt(PropertyUtil.getProperty().getProperty("CLIENT_MONITOR_PORT"));
                    String serverAddress = PropertyUtil.getProperty().getProperty("SERVER_HOSTNAME");
                    String messageID = serverAddress + "/" + ServerRequestIDGenerator.getNextRequestId();

                    ReplyMessage replyMessage = new ReplyMessage("CALLBACK",
                            "", new String(updatedContent), 200, "Success");
                    MessageWrapper wrapperMessage = new MessageWrapper(messageID, replyMessage.getClass().getSimpleName(), replyMessage);

                    byte[] sendBuffer = CustomSerializationUtil.marshal(wrapperMessage);
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
                    socket.send(sendPacket);
                    System.out.println("[" + new Timestamp(System.currentTimeMillis()) + "]" + " Alert sent to " + clientAddress + " for file path: " + fullFilePath);
                } catch (IOException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * This methods check if there are still clients subscribed for the file path
     */
    private void updateClientIntervals(String fullFilePath) {
        List<MonitorClient> clients = registeredClients.get(fullFilePath);
        if (clients != null) {
            unregisterClient(fullFilePath, clients);
        }
    }

    /*
     * This methods updates the monitorInterval field stored for every client in the list and remove them once their intervals becomes less than or equal to 0
     * After removal, we checked if there are still any clients in the list, if not runningThread for this path will be set to false
     * and the running thread will be terminated. The filePath is then removed from the hashmap to save space once unused
     */
    private void unregisterClient(String fullFilePath, List<MonitorClient> clients) {
        clients.removeIf(client -> {
            client.setMonitorInterval(client.getMonitorInterval() - 1); // Decrease by 1 second (simulated time)
            return client.getMonitorInterval() <= 0;
        });
        if (clients.isEmpty()) {
            runningThreads.get(fullFilePath).set(false);
            registeredClients.remove(fullFilePath);
        }
    }
    /*
     * This method obtain the lock of given a file path, if it does not exist, create a new entry and return
     */
    private ReentrantLock getFilePathLock(String fullFilePath) {
        return filePathLocks.computeIfAbsent(fullFilePath, k -> new ReentrantLock());
    }

    /*
     * This method check if the given file path exist on the server directory
     */
    private boolean doesFilePathExist(String fullFilePath) {
        Path path = Paths.get(fullFilePath);
        return Files.exists(path);
    }
}