package com.gigaspaces.gigapro.script_creator.service.script;

import com.gigaspaces.gigapro.script_creator.model.XAPConfigScriptType;
import com.gigaspaces.gigapro.script_creator.service.script.bat.XAPConfigBatScriptCreator;
import com.gigaspaces.gigapro.script_creator.service.script.shell.XAPConfigShellScriptCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class XAPConfigScriptCreatorFactory {

    @Autowired
    private XAPConfigBatScriptCreator batScriptCreator;

    @Autowired
    private XAPConfigShellScriptCreator shellScriptCreator;

    public XAPConfigScriptCreator getXAPConfigScriptCreator(XAPConfigScriptType scriptType) {
        switch (scriptType) {
            case BAT:
                return batScriptCreator;
            case SHELL:
                return shellScriptCreator;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
