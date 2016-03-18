package com.gigaspaces.gigapro.web.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileUtils {
    public static Path createTempFile(String fileName, String fileExtension) {
        try {
            Path tempFile = Files.createTempFile(fileName, fileExtension);
            tempFile.toFile().deleteOnExit();
            return rename(tempFile, fileName + fileExtension);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred creating temp file " + fileName + fileExtension, e);
        }
    }

    public static Path createTempDir(String dirName) {
        try {
            Path tempDirectory = Files.createTempDirectory(dirName);
            tempDirectory.toFile().deleteOnExit();
            return rename(tempDirectory, dirName);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred creating temp directory " + dirName, e);
        }
    }

    /**
     * @param file file or directory to rename
     * @param newNameString new name of file or directory
     * @return the Path to the target file
     * If you try to rename folder, but a non-empty folder with such name already exists,
     * this folder will be cleared and renamed.
     */
    public static Path rename(Path file, String newNameString) {
        try {
            return move(file, file.resolveSibling(newNameString), REPLACE_EXISTING);
        } catch (DirectoryNotEmptyException nee) {
            Path directoryToClear = Paths.get(nee.getMessage());
            try {
                Files.walkFileTree(directoryToClear, new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
                return move(file, file.resolveSibling(newNameString), REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Error occurred renaming " + file.getFileName(), e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error occurred renaming " + file.getFileName(), e);
        }
    }

    /**
     * @param target if target is directory, source will be put to this directory; if target is file, source will replace target
     * @param source file to replace
     */
    public static void replaceFile(Path target, Path source) {
        try {
            Path newDir = isDirectory(target) ? target : target.getParent();
            move(source, newDir.resolve(source.getFileName()), REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred moving file " + source.getFileName(), e);
        }
    }
}
