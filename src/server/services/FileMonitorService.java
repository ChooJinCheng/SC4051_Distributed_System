package server.services;

import message.BaseMessage;
import server.ServerRequestIDGenerator;
import models.MessageWrapper;
import models.MonitorClient;
import utilities.CustomSerializationUtil;
import utilities.FileDataExtractorUtil;
import utilities.FilePathUtil;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/*
 * -ReentrantLock implemented to guard critical sections that interacts with mutual variable among the threads
 * -AtomicBoolean implemented to ensure the change of Boolean for tracking runningThreads can be changed atomically
 * -Each running thread represents monitoring of a specific filePath where multiple clients can be assigned to
 * */
public class FileMonitorService {
    private static final Map<String, List<MonitorClient>> registeredClients = new HashMap<>();
    private static final Map<String, AtomicBoolean> runningThreads = new HashMap<>();
    private static final Map<String, ReentrantLock> filePathLocks = new HashMap<>();
    private static FileMonitorService fileMonitorService;

    private FileMonitorService (){}
    public static synchronized FileMonitorService getInstance() {
        if (fileMonitorService == null) {
            fileMonitorService = new FileMonitorService();
        }
        return fileMonitorService;
    }
    //ToDo: Change return String error to throw error instead
    public String registerClient(String inputFilePath, MonitorClient monitorClient) {
        String fullFilePath = FilePathUtil.getFullPathString(inputFilePath);
        ReentrantLock lock = getFilePathLock(fullFilePath);
        lock.lock();
        try {
            if(!doesFilePathExist(fullFilePath))
                return "Error: File does not exist.";
            registeredClients.computeIfAbsent(fullFilePath, k -> new ArrayList<>()).add(monitorClient);

            if (!runningThreads.containsKey(fullFilePath) || !runningThreads.get(fullFilePath).get()) {
                startMonitoringThread(fullFilePath);
            }
        } finally {
            lock.unlock();
        }
        return "Registration Successful";
    }

    private void startMonitoringThread(String fullFilePath) {
        ReentrantLock lock = getFilePathLock(fullFilePath);
        lock.lock();
        try {
            if (!runningThreads.containsKey(fullFilePath) || !runningThreads.get(fullFilePath).get()) {
                runningThreads.put(fullFilePath, new AtomicBoolean(true));

                Thread monitoringThread = new Thread(() -> {
                    System.out.println("Thread for filepath " + fullFilePath + " has started");
                    FileTime lastModifiedTime = FileDataExtractorUtil.getLastModifiedTime(fullFilePath);
                    while (runningThreads.get(fullFilePath).get()) {
                        try {
                            Thread.sleep(1000);

                            FileTime currentLastModifiedTime = FileDataExtractorUtil.getLastModifiedTime(fullFilePath);
                            if(lastModifiedTime.compareTo(currentLastModifiedTime) < 0){
                                byte[] fileContent = FileDataExtractorUtil.getContentFromFile(fullFilePath);
                                notifyRegisteredClients(fullFilePath, fileContent);
                                lastModifiedTime = currentLastModifiedTime;
                            }
                            updateClientIntervals(fullFilePath);

                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println("Thread for filepath " + fullFilePath + " has terminated");
                });
                monitoringThread.start();
            }
        } finally {
            lock.unlock();
        }
    }

    private void notifyRegisteredClients(String fullFilePath, byte[] updatedContent) {
        List<MonitorClient> clients = registeredClients.get(fullFilePath);
        if (clients != null) {
            for (MonitorClient client : clients) {
                try (DatagramSocket socket = new DatagramSocket()) {
                    InetAddress clientAddress = InetAddress.getByName(client.getClientAddress());
                    int clientPort = client.getClientPort();

                    //ToDo: Update the proper message for notifying client
                    BaseMessage baseMessage = new BaseMessage(ServerRequestIDGenerator.getNextRequestId(),"CALLBACK",
                            "", new String(updatedContent));
                    MessageWrapper wrapperMessage = new MessageWrapper(baseMessage.getClass().getSimpleName(), baseMessage);
                    byte[] sendBuffer = CustomSerializationUtil.marshal(wrapperMessage);
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
                    socket.send(sendPacket);
                } catch (IOException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateClientIntervals(String fullFilePath) {
        List<MonitorClient> clients = registeredClients.get(fullFilePath);
        if (clients != null) {
            unregisterClient(fullFilePath, clients);
        }
    }

    private void unregisterClient(String fullFilePath, List<MonitorClient> clients) {
        if (clients != null) {
            clients.removeIf(client -> {
                client.setMonitorInterval(client.getMonitorInterval() - 1); // Decrease by 1 second (simulated time)
                return client.getMonitorInterval() <= 0;
            });
            if (clients.isEmpty()) {
                runningThreads.get(fullFilePath).set(false);
                registeredClients.remove(fullFilePath);
            }
        }
    }
    private ReentrantLock getFilePathLock(String fullFilePath) {
        return filePathLocks.computeIfAbsent(fullFilePath, k -> new ReentrantLock());
    }

    private boolean doesFilePathExist(String fullFilePath) {
        Path path = Paths.get(fullFilePath);
        return Files.exists(path);
    }
}