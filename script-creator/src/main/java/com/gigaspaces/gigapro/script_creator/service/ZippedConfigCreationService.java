package com.gigaspaces.gigapro.script_creator.service;

import com.gigaspaces.gigapro.script_creator.model.XapConfigOptions;
import com.gigaspaces.gigapro.script_creator.service.script.XAPConfigScriptCreator;
import com.gigaspaces.gigapro.script_creator.service.script.XAPConfigScriptCreatorFactory;
import com.gigaspaces.gigapro.script_creator.service.zip.ZipFileCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.gigaspaces.gigapro.script_creator.util.FileUtils.createTempDir;
import static com.gigaspaces.gigapro.script_creator.util.FileUtils.replaceFile;
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
        List<Path> filesToZip = getFilesToZip(xapConfigOptions, scriptCreator);
        return zipFileCreator.createZipFile(zipConfigName, filesToZip);
    }

    private List<Path> getFilesToZip(XapConfigOptions xapConfigOptions, XAPConfigScriptCreator scriptCreator) {
        Path setAppEnvScript = scriptCreator.createSetAppEnvScript(xapConfigOptions);
        Path webuiScript = scriptCreator.getWebuiScript();
        Path cliScript = scriptCreator.getCliScript();
        Path machineOptionsScript = scriptCreator.getMachineOptionsScript();
        List<Path> startGridScripts = scriptCreator.createStartGridScripts(xapConfigOptions);

        List<Path> toZip = new ArrayList<>();
        toZip.addAll(asList(createTempDir("config"), createTempDir("lib"), createTempDir("logs"), createTempDir("work"), createTempDir("deploy")));
        toZip.add(setAppEnvScript);
        toZip.add(webuiScript);
        toZip.add(cliScript);
        Path grid = createTempDir("grid");
        startGridScripts.forEach(script -> replaceFile(grid, script));
        toZip.add(grid);
        Path local = createTempDir("local");
        replaceFile(local, machineOptionsScript);
        toZip.add(local);
        return toZip;
    }
}
