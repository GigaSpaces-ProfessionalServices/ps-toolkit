package com.gigaspaces.gigapro.web.service.script;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;

import java.nio.file.Path;

public interface XAPConfigScriptCreator {

    Path createSetAppEnvScript(XapConfigOptions options);
    Path getWebuiScript();
    Path getCliScript();
}
