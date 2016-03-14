package com.gigaspaces.gigapro.web.service.script.shell;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.service.script.XAPConfigScriptCreator;
import com.gigaspaces.gigapro.web.util.FileUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class XAPConfigShellScriptCreator implements XAPConfigScriptCreator {
    @Override
    public Path createScript(XapConfigOptions options) throws IOException {
        return FileUtils.createTempFile("setAppEnv", ".sh");
    }
}
