package com.gigaspaces.gigapro.web.service.script.bat;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.service.script.XAPConfigScriptCreator;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class XAPConfigBatScriptCreator implements XAPConfigScriptCreator {
    @Override
    public Path createSetAppEnvScript(XapConfigOptions options) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getWebuiScript() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getCliScript() {
        throw new UnsupportedOperationException();
    }
}
