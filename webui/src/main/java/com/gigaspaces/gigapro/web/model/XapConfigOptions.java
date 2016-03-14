package com.gigaspaces.gigapro.web.model;

import lombok.Data;

/**
 * @author Svitlana_Pogrebna
 *
 */
@Data
public class XapConfigOptions {
    private String javaHome;
    private String xapHome;
    private Integer maxProcessesNumber;
    private Integer maxOpenFileDescriptorsNumber;
    private Boolean isUnicast;
    private Integer discoveryPort;
    private String lookupLocators;
    private String lookupGroups;
    private XAPConfigScriptType scriptType;
}
