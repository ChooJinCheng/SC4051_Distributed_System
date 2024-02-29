package utilities;

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
}
