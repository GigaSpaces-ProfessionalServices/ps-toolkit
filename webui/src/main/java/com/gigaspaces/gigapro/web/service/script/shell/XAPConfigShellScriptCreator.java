package com.gigaspaces.gigapro.web.service.script.shell;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.service.script.XAPConfigScriptCreator;
import com.github.mustachejava.Mustache;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.gigaspaces.gigapro.web.util.FileUtils.createTempFile;
import static java.nio.file.StandardOpenOption.WRITE;

@Service
public class XAPConfigShellScriptCreator implements XAPConfigScriptCreator {

    public static final String SCRIPT_NAME = "setAppEnv";
    public static final String FILE_EXTENSION = ".sh";

    @Resource(name = "shellMustache")
    private Mustache shellMustache;

    @Override
    public Path createScript(XapConfigOptions options) throws IOException {
        Path script = createTempFile(SCRIPT_NAME, FILE_EXTENSION);

        try (BufferedWriter writer = Files.newBufferedWriter(script, WRITE)) {
            shellMustache.execute(writer, options).flush();
            return script;
        }
    }
}
