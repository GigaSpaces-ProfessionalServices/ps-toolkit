package com.gigaspaces.gigapro.script_creator.service;

import com.gigaspaces.gigapro.script_creator.model.XapConfigOptions;

import java.nio.file.Path;

public interface ZippedConfigCreator {

    Path createZippedConfig(XapConfigOptions xapConfigOptions);
}
