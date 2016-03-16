package com.gigaspaces.gigapro.web.service.script.shell;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.service.script.XAPConfigScriptCreator;
import com.github.mustachejava.Mustache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static com.gigaspaces.gigapro.web.util.FileUtils.createTempFile;
import static java.lang.ClassLoader.getSystemResource;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardOpenOption.WRITE;

@Service
public class XAPConfigShellScriptCreator implements XAPConfigScriptCreator {

    @Resource(name = "setAppEnvShellMustache")
    private Mustache setAppEnvShellMustache;

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

    @Override
    public Path createSetAppEnvScript(XapConfigOptions options) throws IOException {
        Path script = createTempFile(setAppEnvScriptName, fileExtension);

        try (BufferedWriter writer = newBufferedWriter(script, WRITE)) {
            setAppEnvShellMustache.execute(writer, options).flush();
            return script;
        }
    }

    @Override
    public Path getWebuiScript() throws IOException, URISyntaxException {
        String scriptPath = staticScriptsPath + webuiScriptName + fileExtension;
        return get(getSystemResource(scriptPath).toURI());
    }

    @Override
    public Path getCliScript() throws IOException, URISyntaxException {
        String scriptPath = staticScriptsPath + cliScriptName + fileExtension;
        return get(getSystemResource(scriptPath).toURI());
    }
}
