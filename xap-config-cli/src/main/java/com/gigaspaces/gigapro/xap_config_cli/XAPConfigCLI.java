package com.gigaspaces.gigapro.xap_config_cli;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;

import static com.gigaspaces.gigapro.xap_config_cli.XAPOptions.HTTP_PORT;
import static com.gigaspaces.gigapro.xap_config_cli.XAPOptions.INITIAL_UNICAST_DISCOVERY_PORT;
import static com.gigaspaces.gigapro.xap_config_cli.XAPOptions.MULTICAST_ANNOUNCEMENT;
import static com.gigaspaces.gigapro.xap_config_cli.XAPOptions.MULTICAST_DISCOVERY_PORT;
import static com.gigaspaces.gigapro.xap_config_cli.XAPOptions.MULTICAST_ENABLED;
import static com.gigaspaces.gigapro.xap_config_cli.XAPOptions.MULTICAST_REQUEST;
import static com.gigaspaces.gigapro.xap_config_cli.XAPOptions.MULTICAST_TTL;
import static com.gigaspaces.gigapro.xap_config_cli.XAPOptions.REGISTRY_PORT;
import static org.apache.commons.cli.Option.builder;

public class XAPConfigCLI {

    /**
     * @param args args from main(String[] args) method
     * @return XAPOptions - the object with parsed properties and wrapped CommandLine object
     * @throws ParseException
     */
    public XAPOptions parseArgs(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(builder(MULTICAST_ENABLED).longOpt("multicast-enabled").desc("Global property allowing you to completely enable or disable multicast in the system.").numberOfArgs(1).type(Boolean.class).build());
        options.addOption(builder(MULTICAST_DISCOVERY_PORT).longOpt("multicast-discoveryPort").desc("The port used during discovery for multicast requests.").numberOfArgs(1).type(Long.class).build());
        options.addOption(builder(MULTICAST_ANNOUNCEMENT).longOpt("multicast-announcement").desc("The multicast address that controls the lookup service announcement. The lookup service uses this address to periodically announce its existence.").numberOfArgs(1).type(String.class).build());
        options.addOption(builder(MULTICAST_REQUEST).longOpt("multicast-request").desc("the multicast address that controls the request of clients (when started) to available lookup services.").numberOfArgs(1).type(String.class).build());
        options.addOption(builder(MULTICAST_TTL).longOpt("multicast-ttl").desc("The multicast packet time to live.").numberOfArgs(1).type(Integer.class).build());
        options.addOption(builder(INITIAL_UNICAST_DISCOVERY_PORT).longOpt("initialUnicastDiscoveryPort").desc("Reggie Lookup Service: in this context, modify com.sun.jini.reggie.initialUnicastDiscoveryPort, the port used during unicast discovery.").numberOfArgs(1).type(Long.class).build());
        options.addOption(builder(REGISTRY_PORT).longOpt("registryPort").desc("RMIRegistry port (used for RMI lookup and for JMX MBean server).").numberOfArgs(1).type(Long.class).build());
        options.addOption(builder(HTTP_PORT).longOpt("httpPort").desc("Webster HTTPD service port.").numberOfArgs(1).type(Long.class).build());

        if (ArrayUtils.isEmpty(args)) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("XAP config options", options, true);
            throw new IllegalArgumentException("Empty arguments");
        }

        return new XAPOptions(parser.parse(options, args));
    }
}
