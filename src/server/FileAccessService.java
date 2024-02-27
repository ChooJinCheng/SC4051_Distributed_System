package server;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;

public class FileAccessService {

    /*
    * Reference: https://docs.oracle.com/javase/8/docs/api/java/io/RandomAccessFile.html
    * RandomAccessFile available modes:
    * "r"	Open for reading only. Invoking any of the write methods of the resulting object will cause an IOException to be thrown.
    * "rw"	Open for reading and writing. If the file does not already exist then an attempt will be made to create it.
    * "rws"	Open for reading and writing, as with "rw", and also require that every update to the file's content or metadata be written synchronously to the underlying storage device.
    * "rwd" Open for reading and writing, as with "rw", and also require that every update to the file's content be written synchronously to the underlying storage device.
    *
    * Path:
    * Client: //Need to provide filePath e.g. "ClientA\\test.txt from client side"
    * FullPath: "currentdirectory\\serverFiles\\ClientA\\test.txt
    * Folder: All files/directories that can be accessed by client will reside within serverFiles directory
    * */

    Path projectPath = Paths.get(System.getProperty("user.dir"));
    Path serverFilePath = projectPath.resolve("server_files");

    public FileAccessService (){

    }
    public String readFileContent(String inputFilePath, long inputOffset, int inputReadLength) {
        //ToDo: Change return String error to throw error instead
        try {
            String filePath = serverFilePath.resolve(inputFilePath).toString();
            Path path = Paths.get("C:\\Users\\jin_c\\IdeaProjects\\SC4051_UDP\\server_files\\test.txt");
            if (!Files.exists(path)) {
                return "Error: File does not exist.";
            }

            long fileSize = Files.size(path);
            if (inputOffset >= fileSize) {
                return "Error: Offset exceeds file length.";
            }

            try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r")) {
                randomAccessFile.seek(inputOffset);

                byte[] contentBytes = new byte[inputReadLength];
                int bytesRead = randomAccessFile.read(contentBytes);

                return new String(contentBytes, 0, bytesRead);
            }
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    public String insertIntoFile(String inputFilePath, long inputOffset, byte[] content) {
        try {
            String filePath = serverFilePath.resolve(inputFilePath).toString();
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return "Error: File does not exist.";
            }

            long fileSize = Files.size(path);
            if (inputOffset > fileSize) {
                return "Error: Offset exceeds file length.";
            }

            try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw")) {
                byte[] originalContent = new byte[(int) (fileSize - inputOffset)];
                randomAccessFile.seek(inputOffset);
                randomAccessFile.read(originalContent);

                randomAccessFile.seek(inputOffset);
                randomAccessFile.write(content);
                randomAccessFile.write(originalContent);

                return "Insertion successful.";
            }
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
