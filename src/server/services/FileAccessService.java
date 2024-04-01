package server.services;

import utilities.FilePathUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;

/*
 * Reference: https://docs.oracle.com/javase/8/docs/api/java/io/RandomAccessFile.html
 * RandomAccessFile available modes:
 * "r"	Open for reading only. Invoking any of the write methods of the resulting object will cause an IOException to be thrown.
 * "rw"	Open for reading and writing. If the file does not already exist then an attempt will be made to create it.
 * "rws"	Open for reading and writing, as with "rw", and also require that every update to the file's content or metadata be written synchronously to the underlying storage device.
 * "rwd" Open for reading and writing, as with "rw", and also require that every update to the file's content be written synchronously to the underlying storage device.
 *
 * Path:
 * Client input: Need to provide filePath e.g. "ClientA\\test.txt" from client side
 * FullPath: currentdirectory\\server_files\\ClientA\\test.txt
 * Folder: All files/directories that can be accessed by client will reside within server_files directory indicated in the config.properties
 *
 * This service class executes read, insert, copy and ....
 * */
public class FileAccessService {
    private static FileAccessService fileAccessService;
    private FileAccessService (){}
    //Ensure only one instance of this service is used among multiple clients, preventing a race condition if multiple client were to access at a single time
    public static synchronized FileAccessService getInstance() {
        if (fileAccessService == null) {
            fileAccessService = new FileAccessService();
        }
        return fileAccessService;
    }

    /*
     * This methods take in the client's input file path, offset and readLength and perform the reading on existing file in the server
     */
    public String readFileContent(String inputFilePath, long inputOffset, int inputReadLength) {
        try {
            //Obtaining full path with the use of FilePathUtil
            String filePathStr = FilePathUtil.getFullPathString(inputFilePath);
            Path filePath = FilePathUtil.getPath(inputFilePath);

            //validity/Boundary checking on user's input to ensure service executes as intended
            if (!Files.exists(filePath)) {
                return "404 Error: File does not exist.";
            }

            long fileSize = Files.size(filePath);
            if(fileSize == 0)
                return "400 Error: There is no content in the file.";

            if (inputOffset >= fileSize)
                return "400 Error: Offset exceeds file length.";


            if (inputReadLength > fileSize)
                return "400 Error: Input read length exceeds file length.";


            //Access the file on the server and read the content as requested by user's input
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePathStr, "r")) {
                randomAccessFile.seek(inputOffset);

                byte[] contentBytes = new byte[inputReadLength];
                int bytesRead = randomAccessFile.read(contentBytes);
                String content = new String(contentBytes, 0, bytesRead);
                return "200".concat(content);
            }
        } catch (IOException e) {
            return "500 Error: " + e.getMessage();
        }
    }

    /*
     * This methods take in the client's input file path, offset and input content and perform an insertion on existing file in the server
     */
    public String insertIntoFile(String inputFilePath, long inputOffset, String content) {
        try {
            String filePathStr = FilePathUtil.getFullPathString(inputFilePath);
            Path filePath = FilePathUtil.getPath(inputFilePath);

            //validity/Boundary checking on user's input to ensure service executes as intended
            if (!Files.exists(filePath)) {
                return "404 Error: File does not exist.";
            }

            long fileSize = Files.size(filePath);
            if (inputOffset > fileSize) {
                return "400 Error: Offset exceeds file length.";
            }

            //Access file with read,write mode and insert the client's input content at specific offset
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePathStr, "rw")) {
                byte[] originalContent = new byte[(int) (fileSize - inputOffset)];
                randomAccessFile.seek(inputOffset);
                randomAccessFile.read(originalContent);

                randomAccessFile.seek(inputOffset);
                randomAccessFile.write(content.getBytes());
                randomAccessFile.write(originalContent);

                return "200 Insertion successful.";
            }
        } catch (IOException e) {
            return "500 Error: " + e.getMessage();
        }
    }

    /*
     * This methods take in the client's input file path, and perform a copy on existing file in the server
     */
    public String copyFile(String inputFilePath){
        try{
            String filePathStr = FilePathUtil.getFullPathString(inputFilePath);
            Path filePath = FilePathUtil.getPath(inputFilePath);
            //validity/Boundary checking on user's input to ensure service executes as intended
            if (!Files.exists(filePath)) {
                return "404 Error: File does not exist.";
            }
            String newFilePathStr =  FilePathUtil.getCopyPathString(inputFilePath);


            // Try to read the existing file content and store them in byte array which is based on its file size
            long fileSize = Files.size(filePath);
            byte[] originalContent = new byte[(int) fileSize];
            try (RandomAccessFile orginalFile = new RandomAccessFile(filePathStr, "r")){
                orginalFile.read(originalContent);
            }
            // Try to write into the a new file based on the byte array
            try(RandomAccessFile newFile = new RandomAccessFile(newFilePathStr, "rw")){
                newFile.write(originalContent);
                return "200 File Copy successful.";
            }

        }catch (IOException e){
            return "500 Error: " + e.getMessage();
        }
    }

    /*
     * This methods take in the client's input file path, and clear content inside an existing file in the server
     */
    public String clearFileContent(String inputFilePath){
        try{
            String filePathStr = FilePathUtil.getFullPathString(inputFilePath);
            Path filePath = FilePathUtil.getPath(inputFilePath);
            //validity/Boundary checking on user's input to ensure service executes as intended
            if (!Files.exists(filePath)) {
                return "404 Error: File does not exist.";
            }

            try(RandomAccessFile newFile = new RandomAccessFile(filePathStr, "rw")){
                newFile.setLength(0); // This clears the content of the file.
                return "200 File Content Clear successful.";
            }

        }catch (IOException e){
            return "500 Error: " + e.getMessage();
        }
    }

}
