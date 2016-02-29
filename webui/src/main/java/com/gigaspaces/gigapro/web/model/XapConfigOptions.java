package com.gigaspaces.gigapro.web.model;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class XapConfigOptions {

    private String javaHome;
    private String xapHome;

    private Integer maxProcessesNumber;
    private Integer maxOpenFileDescriptorsNumber;

    private Boolean isUnicast;
    private Integer discoveryPort;
    private String lookupLocators;
    private String lookupGroups;

    public XapConfigOptions() {
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getXapHome() {
        return xapHome;
    }

    public void setXapHome(String xapHome) {
        this.xapHome = xapHome;
    }

    public Integer getMaxProcessesNumber() {
        return maxProcessesNumber;
    }

    public void setMaxProcessesNumber(Integer maxProcessesNumber) {
        this.maxProcessesNumber = maxProcessesNumber;
    }

    public Integer getMaxOpenFileDescriptorsNumber() {
        return maxOpenFileDescriptorsNumber;
    }

    public void setMaxOpenFileDescriptorsNumber(Integer maxOpenFileDescriptorsNumber) {
        this.maxOpenFileDescriptorsNumber = maxOpenFileDescriptorsNumber;
    }

    public Boolean getIsUnicast() {
        return isUnicast;
    }

    public void setIsUnicast(Boolean isUnicast) {
        this.isUnicast = isUnicast;
    }

    public Integer getDiscoveryPortt() {
        return discoveryPort;
    }

    public void setDiscoveryPort(Integer discoveryPort) {
        this.discoveryPort = discoveryPort;
    }

    public String getLookupLocators() {
        return lookupLocators;
    }

    public void setLookupLocators(String lookupLocators) {
        this.lookupLocators = lookupLocators;
    }

    public String getLookupGroups() {
        return lookupGroups;
    }

    public void setLookupGroups(String lookupGroups) {
        this.lookupGroups = lookupGroups;
    }

    @Override
    public String toString() {
        return "XapConfigOptions [javaHome=" + javaHome + ", xapHome=" + xapHome + ", maxProcessesNumber=" + maxProcessesNumber + ", maxOpenFileDescriptorsNumber=" + maxOpenFileDescriptorsNumber + ", isUnicast=" + isUnicast + ", discoveryPort=" + discoveryPort + ", lookupLocators=" + lookupLocators + ", lookupGroups=" + lookupGroups + "]";
    }
}
