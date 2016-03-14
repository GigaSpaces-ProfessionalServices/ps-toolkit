package com.gigaspaces.gigapro.web.service.script;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;

import java.io.IOException;
import java.nio.file.Path;

public interface XAPConfigScriptCreator {

    Path createScript(XapConfigOptions options) throws IOException;
}
