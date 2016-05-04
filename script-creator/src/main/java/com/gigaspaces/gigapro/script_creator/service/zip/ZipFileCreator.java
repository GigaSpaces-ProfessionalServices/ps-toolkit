package com.gigaspaces.gigapro.script_creator.service.zip;

import java.nio.file.Path;
import java.util.List;

public interface ZipFileCreator {

    Path createZipFile(String name, List<Path> filesToAdd);
}
