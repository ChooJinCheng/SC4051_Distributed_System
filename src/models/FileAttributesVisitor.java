package models;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/*
 * FileVisitor Interface provides methods that are called during the traversal process at different stages: before and after visiting a directory, and before and after visiting a file.
 * FileAttributesVisitor implements FileVisitor to define the behavior during these different stages of traversal.
 *
 * */
public class FileAttributesVisitor implements FileVisitor<Path> {
    private BasicFileAttributes fileAttributes;
    private byte[] fileContent;

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        fileContent = Files.readAllBytes(file);
        fileAttributes = attrs;
        return FileVisitResult.TERMINATE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    public BasicFileAttributes getFileAttributes() {
        return fileAttributes;
    }

    public byte[] getFileContent() {
        return fileContent;
    }
}
