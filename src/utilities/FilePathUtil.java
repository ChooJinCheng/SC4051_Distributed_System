package utilities;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Properties;

/*
 * This utility class implements the construction/manipulation of file paths
 * */
public class FilePathUtil {
    private static final Properties properties = PropertyUtil.getProperty();
    private static final Path projectPath = Paths.get(System.getProperty("user.dir"));
    private static final Path serverFilePath = projectPath.resolve(properties.getProperty("SERVER_FILE_DIR"));

    /*
     * This method concatenate the server file path with the user's given file path
     * */
    public static String getFullPathString(String inputFilePath){
        return serverFilePath.resolve(inputFilePath).toString();
    }
    /*
     * This method return the actual path given the file path String
     * */
    public static Path getPath(String inputFilePath){
        String filePathStr = getFullPathString(inputFilePath);
        return Paths.get(filePathStr);
    }


    /*
     * This method takes in a file path and generates a new file path for creating a copy of an existing file,
     * and ensures the new path does not conflict with existing files.
     * */
    public static String getCopyPathString(String inputFilePath) {
        //Extract the original path and then get the file name
        Path originalPath = getPath(inputFilePath);
        String fileName = originalPath.getFileName().toString();

        // Finds the index of the last period (.) in the file name, which typically separates the file name from its extension.
        int dotIndex = fileName.lastIndexOf('.');
        String baseName;
        String extensionType = "";

        // If dotIndex is greater than 0, it splits the fileName into baseName and extensionType
        if  (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            extensionType = fileName.substring(dotIndex); // eg .txt, .pdf
        // else if there is no . or it's the first character (files starting with . but without an extension,)
        // Then the entire fileName is treated as baseName with no extensionType
        } else {
            // eg .gitignore
            baseName = fileName;
        }

        // Create the file name for the copy
        String modifiedFileName = baseName + " - Copy" + extensionType;
        // Then create the file path for the copy
        Path modifiedPath = originalPath.getParent().resolve(modifiedFileName);

        // Check if the file exists and create a new name if necessary
        int copyNumber = 1;
        while (Files.exists(modifiedPath)) {
            ++copyNumber;
            modifiedFileName = baseName + " - Copy (" + copyNumber + ")" + extensionType;
            modifiedPath = originalPath.getParent().resolve(modifiedFileName);
        }

        return modifiedPath.toString();
    }
}
