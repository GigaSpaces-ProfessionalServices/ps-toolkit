package com.gigaspaces.gigapro.web.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ValidationException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.*;

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
    private List<ZoneConfig> zoneOptions;

    /**
     * This method returns lookupLocators as list.
     *
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
     *
     * @return If lookupLocators = "10.9.20.1, 10.9.20.2, 10.9.20.3" <br/>
     * and discoveryPort = "8888",<br/> it'll return "10.9.20.1:8888,10.9.20.2:8888,10.9.20.3:8888".
     */
    public String getLookupLocatorsWithPort() {
        List<String> lookupLocatorsWithPort = getLookupLocatorsAsList().stream().map(i -> i + ":" + discoveryPort).collect(toList());
        return join(lookupLocatorsWithPort, ",");
    }


    /**
     * This method validates all required fields for @NotNull
     */
    public void validate() {
        String errorMessage = "";
        if (isBlank(javaHome)) {
            errorMessage += "javaHome cannot be null or empty!<br/>";
        }
        if (isBlank(xapHome)) {
            errorMessage += "xapHome cannot be null or empty!<br/>";
        }
        if (isBlank(lookupGroups)) {
            errorMessage += "lookupGroups cannot be null or empty!<br/>";
        }
        if (maxProcessesNumber == null) {
            errorMessage += "maxProcessesNumber cannot be null!<br/>";
        }
        if (maxOpenFileDescriptorsNumber == null) {
            errorMessage += "maxOpenFileDescriptorsNumber cannot be null!<br/>";
        }
        if (scriptType == null) {
            errorMessage += "scriptType cannot be null!<br/>";
        }
        if (isTrue(isUnicast)) {
            if (discoveryPort == null) {
                errorMessage += "discoveryPort cannot be null!<br/>";
            }
            if (isBlank(lookupLocators))
                errorMessage += "lookupLocators cannot be null or empty!<br/>";
        }
        if (zoneOptions != null) {
            for (ZoneConfig zone : zoneOptions) {
                try {
                    zone.validate();
                } catch (RuntimeException e) {
                    errorMessage += e.getMessage();
                }
            }
        } else {
            errorMessage += "zoneOptions cannot be null or empty!<br/>";
        }
        if (StringUtils.isNotEmpty(errorMessage)) {
            throw new ValidationException(errorMessage);
        }
    }
}
