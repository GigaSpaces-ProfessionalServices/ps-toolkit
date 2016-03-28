package com.gigaspaces.gigapro.web.service.script.shell;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.model.ZoneConfig;
import com.gigaspaces.gigapro.web.service.script.XAPConfigScriptCreator;
import com.github.mustachejava.Mustache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.gigaspaces.gigapro.web.util.FileUtils.createTempFile;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class XAPConfigShellScriptCreator implements XAPConfigScriptCreator {

    @Resource(name = "setAppEnvShellMustache")
    private Mustache setAppEnvShellMustache;

    @Resource(name = "startGridShellMustache")
    private Mustache startGridShellMustache;

    @Value("${app.scripts.static-scripts.path}")
    private String staticScriptsPath;

    @Value("${app.scripts.extension.shell}")
    private String fileExtension;

    @Value("${app.scripts.set-app-env.name}")
    private String setAppEnvScriptName;

    @Value("${app.scripts.web-ui.name}")
    private String webuiScriptName;

    @Value("${app.scripts.cli.name}")
    private String cliScriptName;

    @Value("${app.scripts.start-grid.name}")
    private String startGridScriptName;

    @Override
    public Path createSetAppEnvScript(XapConfigOptions options) {
        Path script = createTempFile(setAppEnvScriptName, fileExtension);

        try (BufferedWriter writer = newBufferedWriter(script, WRITE)) {
            setAppEnvShellMustache.execute(writer, options).flush();
            return script;
        } catch (IOException e) {
            throw new RuntimeException("Error occurred generating script " + setAppEnvScriptName + fileExtension, e);
        }
    }

    @Override
    public Path getWebuiScript() {
        return getStaticScript(webuiScriptName);
    }

    @Override
    public Path getCliScript() {
        return getStaticScript(cliScriptName);
    }

    @Override
    public List<Path> createStartGridScripts(XapConfigOptions options) {
        List<Path> startGridScripts = new ArrayList<>();
        for (ZoneConfig zone : options.getZoneOptions()) {
            String scriptName = isNotBlank(zone.getZoneName()) ? "start-" + zone.getZoneName() + "-services" : startGridScriptName;
            Path script = createTempFile(scriptName, fileExtension);
            try (BufferedWriter writer = newBufferedWriter(script, WRITE)) {
                startGridShellMustache.execute(writer, zone).flush();
            } catch (IOException e) {
                throw new RuntimeException("Error occurred generating script " + scriptName + fileExtension, e);
            }
            startGridScripts.add(script);
        }
        return startGridScripts;
    }

    private Path getStaticScript(String scriptName) {
        try {
            String scriptPath = "/" + staticScriptsPath + scriptName + fileExtension;
            Path script = createTempFile(scriptName, fileExtension);
            Files.copy(getClass().getResourceAsStream(scriptPath), script, REPLACE_EXISTING);
            return script;
        } catch (IOException e) {
            throw new RuntimeException("Error occurred generating script " + scriptName + fileExtension, e);
        }
    }
}
