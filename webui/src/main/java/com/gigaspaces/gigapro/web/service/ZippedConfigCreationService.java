package com.gigaspaces.gigapro.web.service;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.service.script.XAPConfigScriptCreator;
import com.gigaspaces.gigapro.web.service.script.XAPConfigScriptCreatorFactory;
import com.gigaspaces.gigapro.web.service.zip.ZipFileCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.gigaspaces.gigapro.web.util.FileUtils.createTempDir;
import static java.util.Arrays.asList;

@Service
public class ZippedConfigCreationService implements ZippedConfigCreator {

    @Autowired
    private ZipFileCreator zipFileCreator;

    @Autowired
    private XAPConfigScriptCreatorFactory scriptCreatorFactory;

    @Value("${app.zipped.config.name}")
    private String zipConfigName;

    @Override
    public Path createZippedConfig(XapConfigOptions xapConfigOptions) throws IOException {
        XAPConfigScriptCreator scriptCreator = scriptCreatorFactory.getXAPConfigScriptCreator(xapConfigOptions.getScriptType());
        Path script = scriptCreator.createScript(xapConfigOptions);
        List<Path> filesToZip = asList(createTempDir("config"), createTempDir("lib"), createTempDir("local"), createTempDir("logs"), createTempDir("work"), createTempDir("deploy"), script);
        return zipFileCreator.createZipFile(zipConfigName, filesToZip);
    }
}
