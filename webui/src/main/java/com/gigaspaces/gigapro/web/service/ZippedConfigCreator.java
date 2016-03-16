package com.gigaspaces.gigapro.web.service;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public interface ZippedConfigCreator {

    Path createZippedConfig(XapConfigOptions xapConfigOptions) throws IOException, URISyntaxException;
}
