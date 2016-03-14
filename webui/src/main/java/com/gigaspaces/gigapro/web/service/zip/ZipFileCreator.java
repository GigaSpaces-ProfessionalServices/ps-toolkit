package com.gigaspaces.gigapro.web.service.zip;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ZipFileCreator {

    Path createZipFile(String name, List<Path> filesToAdd) throws IOException;
}
