package com.gigaspaces.gigapro.web.service.script;

import com.gigaspaces.gigapro.web.model.XAPConfigScriptType;
import com.gigaspaces.gigapro.web.service.script.bat.XAPConfigBatScriptCreator;
import com.gigaspaces.gigapro.web.service.script.shell.XAPConfigShellScriptCreator;
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
