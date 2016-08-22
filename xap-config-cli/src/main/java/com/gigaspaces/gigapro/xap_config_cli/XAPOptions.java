package com.gigaspaces.gigapro.xap_config_cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.util.Optional;

public class XAPOptions {
    public static final String MULTICAST_ENABLED = "e";
    public static final String MULTICAST_DISCOVERY_PORT = "d";
    public static final String MULTICAST_ANNOUNCEMENT = "a";
    public static final String MULTICAST_REQUEST = "r";
    public static final String MULTICAST_TTL = "t";
    public static final String INITIAL_UNICAST_DISCOVERY_PORT = "i";
    public static final String REGISTRY_PORT = "R";
    public static final String HTTP_PORT = "h";

    private CommandLine commandLine;

    public XAPOptions(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public Optional<Boolean> getMulticastEnabled() {
        try {
            return Optional.ofNullable((Boolean) commandLine.getParsedOptionValue(MULTICAST_ENABLED));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    public Optional<Long> getMulticastDiscoveryPort() {
        try {
            return Optional.ofNullable((Long) commandLine.getParsedOptionValue(MULTICAST_DISCOVERY_PORT));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    public Optional<String> getMulticastAnnouncement() {
        return Optional.ofNullable(commandLine.getOptionValue(MULTICAST_ANNOUNCEMENT));
    }

    public Optional<String> getMulticastRequest() {
        return Optional.ofNullable(commandLine.getOptionValue(MULTICAST_REQUEST));
    }

    public Optional<Integer> getMulticastTtl() {
        try {
            return Optional.ofNullable((Integer) commandLine.getParsedOptionValue(MULTICAST_TTL));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    public Optional<Long> getInitialUnicastDiscoveryPort() {
        try {
            return Optional.ofNullable((Long) commandLine.getParsedOptionValue(INITIAL_UNICAST_DISCOVERY_PORT));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    public Optional<Long> getRegistryPort() {
        try {
            return Optional.ofNullable((Long) commandLine.getParsedOptionValue(REGISTRY_PORT));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    public Optional<Long> getHttpPort() {
        try {
            return Optional.ofNullable((Long) commandLine.getParsedOptionValue(HTTP_PORT));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }
}
