package com.gigaspaces.gigapro.script_creator.service.script.bat;

import com.gigaspaces.gigapro.script_creator.model.XapConfigOptions;
import com.gigaspaces.gigapro.script_creator.service.script.XAPConfigScriptCreator;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

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

    @Override
    public Path getMachineOptionsScript() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Path> createStartGridScripts(XapConfigOptions options) {
        throw new UnsupportedOperationException();
    }
}
