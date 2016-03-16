package com.gigaspaces.gigapro.web.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public static Path rename(Path file, String newNameString) {
        try {
            return Files.move(file, file.resolveSibling(newNameString), REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred renaming file " + file.getFileName(), e);
        }
    }
}
