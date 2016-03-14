package com.gigaspaces.gigapro.web.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileUtils {
    public static Path createTempFile(String fileName, String fileExtension) throws IOException {
        Path tempFile = Files.createTempFile(fileName, fileExtension);
        tempFile.toFile().deleteOnExit();
        return rename(tempFile, fileName + fileExtension);
    }

    public static Path createTempDir(String dirName) throws IOException {
        Path tempDirectory = Files.createTempDirectory(dirName);
        tempDirectory.toFile().deleteOnExit();
        return rename(tempDirectory, dirName);
    }

    public static Path rename(Path file, String newNameString) throws IOException {
        return Files.move(file, file.resolveSibling(newNameString), REPLACE_EXISTING);
    }
}
