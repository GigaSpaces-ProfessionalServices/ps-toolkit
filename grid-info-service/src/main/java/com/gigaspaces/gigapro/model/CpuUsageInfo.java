package com.gigaspaces.gigapro.model;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class CpuUsageInfo {
    
    private String hostName;
    private String hostAddress;
    private String operationSystem;
    private int availableProcessors;
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
    public String getOperationSystem() {
        return operationSystem;
    }
    public void setOperationSystem(String operationSystem) {
        this.operationSystem = operationSystem;
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
