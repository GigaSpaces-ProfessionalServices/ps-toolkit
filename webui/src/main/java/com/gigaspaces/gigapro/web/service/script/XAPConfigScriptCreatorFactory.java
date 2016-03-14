package com.gigaspaces.gigapro.web.service.script;

import com.gigaspaces.gigapro.web.model.XAPConfigScriptType;
import com.gigaspaces.gigapro.web.service.script.bat.XAPConfigBatScriptCreator;
import com.gigaspaces.gigapro.web.service.script.shell.XAPConfigShellScriptCreator;
import org.springframework.stereotype.Component;

@Component
public class XAPConfigScriptCreatorFactory {

    private XAPConfigBatScriptCreator batScriptCreator = new XAPConfigBatScriptCreator();

    private XAPConfigShellScriptCreator shellScriptCreator = new XAPConfigShellScriptCreator();

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
