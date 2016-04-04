package com.gigaspaces.gigapro.rebalancer.config;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CommaParameterSplitter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Skyler on 3/17/2016.
 */
@Getter
@Setter
public class Configuration {

    @Parameter(names = {"-p", "--processing-unit"}, description = "Name of the processing unit to rebalance.", required = true)
    private String name = "space";

    @Parameter(names = {"-l", "--locators"}, description = "A comma separated list of XAP lookup locators for the target grid.", splitter = CommaParameterSplitter.class)
    private List<String> locators = new ArrayList<>();

    @Parameter(names = {"-g", "--groups"}, description = "A comma separated list of XAP lookup groups for the target grid.")
    private List<String> groups = new ArrayList<>();

    @Parameter(names = {"-s", "--secured"}, description = "Makes the command interactive and prompts for username and password.")
    private boolean secured;

    @Parameter(names = {"-t", "--timeout"}, description = "Value used for system timeouts measured in milliseconds.")
    private int timeout = 30000;

    @Parameter(names = {"-m", "--machines"}, required = true, description = "The number of machines capable of running the target processing unit.")
    private int machines = 1;

    @Override
    public String toString() {
        return String.format("[Processing Unit: %s, Locators: %s, Groups: %s, Timeout: %s, Secured: %s, Machines: %s]", name, locators, groups, timeout, secured, machines);
    }
}

