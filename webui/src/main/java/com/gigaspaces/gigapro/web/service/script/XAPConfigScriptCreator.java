package com.gigaspaces.gigapro.web.service.script;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public interface XAPConfigScriptCreator {

    Path createSetAppEnvScript(XapConfigOptions options) throws IOException;
    Path getWebuiScript() throws IOException, URISyntaxException;
    Path getCliScript() throws IOException, URISyntaxException;
}
