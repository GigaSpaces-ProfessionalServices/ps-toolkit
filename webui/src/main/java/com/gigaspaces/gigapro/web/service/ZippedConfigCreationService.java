package com.gigaspaces.gigapro.web.service;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.service.script.XAPConfigScriptCreator;
import com.gigaspaces.gigapro.web.service.script.XAPConfigScriptCreatorFactory;
import com.gigaspaces.gigapro.web.service.zip.ZipFileCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Value("${app.zipped-config.name}")
    private String zipConfigName;

    @Override
    public Path createZippedConfig(XapConfigOptions xapConfigOptions) {
        XAPConfigScriptCreator scriptCreator = scriptCreatorFactory.getXAPConfigScriptCreator(xapConfigOptions.getScriptType());
        Path setAppEnvScript = scriptCreator.createSetAppEnvScript(xapConfigOptions);
        Path webuiScript = scriptCreator.getWebuiScript();
        Path cliScript = scriptCreator.getCliScript();
        List<Path> filesToZip = asList(
                createTempDir("config"), createTempDir("lib"), createTempDir("local"), createTempDir("logs"), createTempDir("work"), createTempDir("deploy"),
                setAppEnvScript, webuiScript, cliScript);
        return zipFileCreator.createZipFile(zipConfigName, filesToZip);
    }
}
