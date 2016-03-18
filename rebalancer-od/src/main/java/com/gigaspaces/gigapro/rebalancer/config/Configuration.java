package com.gigaspaces.gigapro.rebalancer.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CommaParameterSplitter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Skyler on 3/17/2016.
 */
public class Configuration {

    @Parameter(names = {"-p", "--processing-unit"}, description = "Name of the processing unit to rebalance.", required = true)
    private String name = "space";

    @Parameter(names = {"-l", "--locators"}, description = "A comma separated list of XAP lookup locators for the target grid.", splitter = CommaParameterSplitter.class)
    private List<String> locators = new ArrayList<>();

    @Parameter(names = {"-g", "--groups"}, description = "A comma separated list of XAP lookup groups for the target grid.")
    private List<String> groups = new ArrayList<>();

    @Parameter(names = {"-s", "--secured"}, description = "Makes the command interactive and prompts for username and password.")
    private boolean secured;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Parameter(names = {"-t", "--timeout"}, description = "Value used for system timeouts measured in milliseconds.")
    private int timeout = 30000;

    @Parameter(names = {"-m", "--machines"}, required = true, description = "The number of machines capable of running the target processing unit.")
    private int machines = 1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLocators() {
        return locators;
    }

    public void setLocators(List<String> locators) {
        this.locators = locators;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    @Override
    public String toString() {
        return String.format("[Processing Unit: %s, Locators: %s, Groups: %s, Timeout: %s, Secured: %s]", name, locators, groups, timeout, secured);
    }

    public int getMachines() {
        return machines;
    }

    public void setMachines(int machines) {
        this.machines = machines;
    }
}

