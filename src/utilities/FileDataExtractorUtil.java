package utilities;

import models.FileAttributesVisitor;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.EnumSet;
import java.util.Properties;

/*
 * To define the behaviour of file traversal, amend in FileAttributesVisitor.java
 *
 *
 * */
public class FileDataExtractorUtil {
    public static FileTime getLastModifiedTime(String fullFilePath){
        BasicFileAttributes basicFileAttributes = getMetadataFromFile(fullFilePath);
        return basicFileAttributes.lastModifiedTime();
    }

    public static byte[] getContentFromFile(String fullFilePath) {
        FileAttributesVisitor visitor = getFileAttributeVisitor(fullFilePath);
        return visitor.getFileContent();
    }

    private static BasicFileAttributes getMetadataFromFile(String fullFilePath) {
        FileAttributesVisitor visitor = getFileAttributeVisitor(fullFilePath);
        return visitor.getFileAttributes();
    }

    private static FileAttributesVisitor getFileAttributeVisitor(String fullFilePath) {
        try {
            Path filePath = FilePathUtil.getPath(fullFilePath);

            FileAttributesVisitor visitor = new FileAttributesVisitor();
            Files.walkFileTree(filePath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, visitor);

            return visitor;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
