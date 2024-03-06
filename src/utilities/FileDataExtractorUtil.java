package utilities;

import models.FileAttributesVisitor;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.EnumSet;

/*
 * This utility class implements file content and metadata extraction of a given file path
 * To define the behaviour of file traversal, amend in FileAttributesVisitor.java
 * */
public class FileDataExtractorUtil {
    /*
     * This method obtain the lastModifiedTime of a file
     * */
    public static FileTime getLastModifiedTime(String fullFilePath){
        BasicFileAttributes basicFileAttributes = getMetadataFromFile(fullFilePath);
        return basicFileAttributes.lastModifiedTime();
    }
    // make it easier for client side to do time manipulation (unix seconds)
    public static long getLastModifiedTimeInUnix(String fullFilePath) {
        BasicFileAttributes basicFileAttributes = getMetadataFromFile(fullFilePath);
        return basicFileAttributes.lastModifiedTime().toInstant().getEpochSecond();
    }
    /*
     * This method obtain the content of a file
     * */
    public static byte[] getContentFromFile(String fullFilePath) {
        FileAttributesVisitor visitor = getFileAttributeVisitor(fullFilePath);
        return visitor.getFileContent();
    }
    /*
     * This method obtain all the file attributes of a given file path
     * */
    private static BasicFileAttributes getMetadataFromFile(String fullFilePath) {
        FileAttributesVisitor visitor = getFileAttributeVisitor(fullFilePath);
        return visitor.getFileAttributes();
    }

    /*
     * This method make use of FileAttributesVisitor class to traverse the file tree using the Files method
     * and return a visitor class that contains data that can be defined in the FileAttributesVisitor class
     * */
    private static FileAttributesVisitor getFileAttributeVisitor(String fullFilePath) {
        try {
            Path filePath = FilePathUtil.getPath(fullFilePath);

            //Create an instance of a visitor that have it's behaviour defined to obtain data when traversing the tree
            FileAttributesVisitor visitor = new FileAttributesVisitor();
            //Traverse the tree with given file, EnumSet of files you want to visit, maxDepth of the tree and the visitor to collect data
            Files.walkFileTree(filePath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, visitor);

            return visitor;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
