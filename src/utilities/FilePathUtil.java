package utilities;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Properties;

public class FilePathUtil {
    private static final Properties properties = PropertyUtil.getProperty();
    private static final Path projectPath = Paths.get(System.getProperty("user.dir"));
    private static final Path serverFilePath = projectPath.resolve(properties.getProperty("SERVER_FILE_DIR"));

    public static String getFullPathString(String inputFilePath){
        return serverFilePath.resolve(inputFilePath).toString();
    }

    public static Path getPath(String inputFilePath){
        String filePathStr = getFullPathString(inputFilePath);
        return Paths.get(filePathStr);
    }

    public static String getCopyPathString(String inputFilePath) {
        Path originalPath = getPath(inputFilePath);
        String fileName = originalPath.getFileName().toString();

        int dotIndex = fileName.lastIndexOf('.');
        String baseName;
        String extensionType = "";

        if (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            extensionType = fileName.substring(dotIndex); // eg .txt, .pdf
        } else {
            // eg .gitignore
            baseName = fileName;
        }

        String modifiedFileName = baseName + " - Copy" + extensionType;
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
