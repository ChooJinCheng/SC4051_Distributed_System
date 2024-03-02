package server.services;

import utilities.FilePathUtil;
import utilities.PropertyUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.Properties;

/*
 * Reference: https://docs.oracle.com/javase/8/docs/api/java/io/RandomAccessFile.html
 * RandomAccessFile available modes:
 * "r"	Open for reading only. Invoking any of the write methods of the resulting object will cause an IOException to be thrown.
 * "rw"	Open for reading and writing. If the file does not already exist then an attempt will be made to create it.
 * "rws"	Open for reading and writing, as with "rw", and also require that every update to the file's content or metadata be written synchronously to the underlying storage device.
 * "rwd" Open for reading and writing, as with "rw", and also require that every update to the file's content be written synchronously to the underlying storage device.
 *
 * Path:
 * Client: //Need to provide filePath e.g. "ClientA\\test.txt" from client side
 * FullPath: "currentdirectory\\server_files\\ClientA\\test.txt
 * Folder: All files/directories that can be accessed by client will reside within serverFiles directory
 * */
public class FileAccessService {
    //ToDo: Change return String error to throw error instead
    private static FileAccessService fileAccessService;
    private FileAccessService (){}
    public static synchronized FileAccessService getInstance() {
        if (fileAccessService == null) {
            fileAccessService = new FileAccessService();
        }
        return fileAccessService;
    }
    public String readFileContent(String inputFilePath, long inputOffset, int inputReadLength) {
        try {
            String filePathStr = FilePathUtil.getFullPathString(inputFilePath);
            Path filePath = FilePathUtil.getPath(inputFilePath);
            if (!Files.exists(filePath)) {
                return "Error: File does not exist.";
            }

            long fileSize = Files.size(filePath);
            if (inputOffset >= fileSize) {
                return "Error: Offset exceeds file length.";
            }

            try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePathStr, "r")) {
                randomAccessFile.seek(inputOffset);

                byte[] contentBytes = new byte[inputReadLength];
                int bytesRead = randomAccessFile.read(contentBytes);

                return new String(contentBytes, 0, bytesRead);
            }
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    public String insertIntoFile(String inputFilePath, long inputOffset, String content) {
        try {
            String filePathStr = FilePathUtil.getFullPathString(inputFilePath);
            Path filePath = FilePathUtil.getPath(inputFilePath);
            if (!Files.exists(filePath)) {
                return "Error: File does not exist.";
            }

            long fileSize = Files.size(filePath);
            if (inputOffset > fileSize) {
                return "Error: Offset exceeds file length.";
            }

            try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePathStr, "rw")) {
                byte[] originalContent = new byte[(int) (fileSize - inputOffset)];
                randomAccessFile.seek(inputOffset);
                randomAccessFile.read(originalContent);

                randomAccessFile.seek(inputOffset);
                randomAccessFile.write(content.getBytes());
                randomAccessFile.write(originalContent);

                return "Insertion successful.";
            }
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
