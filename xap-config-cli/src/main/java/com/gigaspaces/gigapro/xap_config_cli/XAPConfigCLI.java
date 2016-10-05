package com.gigaspaces.gigapro.xap_config_cli;

import org.apache.commons.cli.ParseException;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Set;

import static org.apache.commons.cli.Option.builder;

public class XAPConfigCLI {

    private Options options = new Options();

    /**
     * Adds a program argument description to be parsed.
     */
    public XAPConfigCLI addOption(String name, String shortOption, String longOption, String description, int numOfArgs, Class<?> type) {
        options.addOption(builder(shortOption).argName(name).longOpt(longOption).desc(description).numberOfArgs(numOfArgs).type(type).build());
        return this;
    }

    /**
     * Adds program argument descriptions to be parsed.
     */
    public XAPConfigCLI addXapOptions(Set<XapOption> xapOptions) {
        if (xapOptions != null) {
            for (XapOption option : xapOptions) {
                addOption(option.name().toLowerCase(), option.getShortOption(), option.getLongOption(), option.getDescription(), option.getNumberOfArg(), option.getType());
            }
        }
        return this;
    }

    /**
     * Parses program arguments
     *
     * @param args
     *            args from main(String[] args) method
     * @return CommandLine object
     * @throws ParseException
     */
    public CommandLine parseArgs(String[] args) throws ParseException {
        try {
            CommandLineParser parser = new DefaultParser();
            if (ArrayUtils.isEmpty(args)) {
                printHelp();
            }

            CommandLine cl = parser.parse(options, args);
            if (cl.hasOption(XapOption.HELP.getShortOption())) {
                printHelp();
            }
            return cl;

        } catch (ParseException e) {
            printHelp();
        }
        return null;
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("XAP config options", options, true);
        System.exit(1);
    }

}
