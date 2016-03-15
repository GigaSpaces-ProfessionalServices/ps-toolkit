package com.gigaspaces.gigapro.web.model;

import lombok.Data;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.split;

/**
 * @author Svitlana_Pogrebna
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

    /**
     * This method returns lookupLocators as list.
     * @return List of lookupLocators items.<br/>
     * If lookupLocators = "10.9.20.1, 10.9.20.2, 10.9.20.3" <br/>
     * it'll return {"10.9.20.1", "10.9.20.2", "10.9.20.3"}.
     */
    public List<String> getLookupLocatorsAsList() {
        String[] lookupLocatorsArray = split(lookupLocators, ",");
        return isNotEmpty(lookupLocatorsArray) ? asList(lookupLocatorsArray).stream().map(String::trim).collect(toList()) : emptyList();
    }

    /**
     * This method returns lookupLocators with port. It's needed in order to use it in mustache template.
     * @return
     * If lookupLocators = "10.9.20.1, 10.9.20.2, 10.9.20.3" <br/>
     * and discoveryPort = "8888",<br/> it'll return "10.9.20.1:8888,10.9.20.2:8888,10.9.20.3:8888".
     */
    public String getLookupLocatorsWithPort() {
        List<String> lookupLocatorsWithPort = getLookupLocatorsAsList().stream().map(i -> i + ":" + discoveryPort).collect(toList());
        return join(lookupLocatorsWithPort, ",");
    }
}
