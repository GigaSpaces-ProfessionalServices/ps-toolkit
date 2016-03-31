package com.gigaspaces.gigapro.web.service.script;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;

import java.nio.file.Path;
import java.util.List;

public interface XAPConfigScriptCreator {

    Path createSetAppEnvScript(XapConfigOptions options);
    Path getWebuiScript();
    Path getCliScript();
    Path getMachineOptionsScript();
    List<Path> createStartGridScripts(XapConfigOptions options);
}
