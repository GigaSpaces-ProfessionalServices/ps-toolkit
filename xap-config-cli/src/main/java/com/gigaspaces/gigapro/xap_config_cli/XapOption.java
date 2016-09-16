package com.gigaspaces.gigapro.xap_config_cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.util.Optional;

/**
 * @author Svitlana_Pogrebna
 *
 */
public enum XapOption {
    HELP("h", "help", "Displays usage", 0, null),
    
    LOOKUP_GROUPS("g", "lookup-groups", "The group used for multicast lookup discovery", 1, String.class),
    LOOKUP_LOCATORS("l", "lookup-locators", "A comma separated list of host:port for unicast lookup discovery", 1, String.class),
    USERNAME("u", "username", "Username for discovery of secured services", 1, String.class),
    RMI_SERVER_HOSTNAME("s", "rmi-server-hostname", "Resolves the NIC address for all services, which bind to a specific network interface", 1, String.class),
    
    // MULTICAST LOOKUP SERVICE OPTIONS
    MULTICAST("e", "multicast-enabled", "Global property allowing you to completely enable or disable multicast in the system.", 1, Boolean.class),
    MULTICAST_DISCOVERY("d", "multicast-discoveryPort", "The port used during discovery for multicast requests.", 1, Long.class),
    MULTICAST_ANNOUNCEMENT("a", "multicast-announcement", "The multicast address that controls the lookup service announcement. The lookup service uses this address to periodically announce its existence.", 1, String.class),
    MULTICAST_REQUEST("r", "multicast-request", "The multicast address that controls the request of clients (when started) to available lookup services.", 1, String.class),
    MULTICAST_TTL("t", "multicast-ttl", "The multicast packet time to live.", 1, Number.class),
    INITIAL_UNICAST_DISCOVERY_PORT("i", "initialUnicastDiscoveryPort", "Reggie Lookup Service: in this context, modify com.sun.jini.reggie.initialUnicastDiscoveryPort, the port used during unicast discovery.", 1, Long.class),
    REGISTRY_PORT("R", "registryPort", "RMIRegistry port (used for RMI lookup and for JMX MBean server).", 1, Long.class),
    HTTP_PORT("h", "httpPort", "Webster HTTPD service port.", 1, Long.class);
    
    private final String shortOption;
    private final String longOption;
    private final String description;
    private final int numberOfArg;
    private final Class<?> type;
    
    private XapOption(String shortOption, String longOption, String description, int numberOfArg, Class<?> type) {
        this.shortOption = shortOption;
        this.longOption = longOption;
        this.description = description;
        this.numberOfArg = numberOfArg;
        this.type = type;
    }

    public String getShortOption() {
        return shortOption;
    }

    public String getLongOption() {
        return longOption;
    }

    public String getDescription() {
        return description;
    }

    public int getNumberOfArg() {
        return numberOfArg;
    }

    public Class<?> getType() {
        return type;
    }
    
    @SuppressWarnings("unchecked")
    public <T> Optional<T> value(CommandLine commandLine) {
        try {
            return Optional.ofNullable((T) commandLine.getParsedOptionValue(getShortOption()));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }
}
