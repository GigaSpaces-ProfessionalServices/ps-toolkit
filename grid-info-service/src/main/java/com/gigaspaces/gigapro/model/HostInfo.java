package com.gigaspaces.gigapro.model;

import com.gigaspaces.gigapro.convert.property.PropertyKey;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class HostInfo {

    @PropertyKey("host_name")
    private String hostName;
    @PropertyKey("host_address")
    private String hostAddress;
    @PropertyKey("operation_system_name")
    private String osName;
    @PropertyKey("operation_system_version")
    private String osVersion;
    @PropertyKey("operation_system_arch")
    private String osArch;
    @PropertyKey("available_processors")
    private int availableProcessors;
    @PropertyKey("RAM[MB]")
    private double physicalMemorySizeInMB;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsArch() {
        return osArch;
    }

    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public void setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    public double getPhysicalMemorySizeInMB() {
        return physicalMemorySizeInMB;
    }

    public void setPhysicalMemorySizeInMB(double physicalMemorySizeInMB) {
        this.physicalMemorySizeInMB = physicalMemorySizeInMB;
    }
}
