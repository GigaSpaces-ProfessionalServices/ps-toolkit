package com.gigaspaces.gigapro.xap_config_cli;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Set;

import static org.apache.commons.cli.Option.builder;

public class XAPConfigCLI {

    private Options options = new Options();

    private static final int HELP_WIDTH = 130;

    /**
     * Adds a program argument description to be parsed.
     * 
     * @see <a
     *      href="http://commons.apache.org/proper/commons-cli/javadocs/api-release/org/apache/commons/cli/PatternOptionBuilder.html">PatternOptionBuilder</a>
     *      for supported type
     * 
     */
    public XAPConfigCLI addOption(String name, String shortOption, String longOption, String description, int numOfArgs, Class<?> type) {
        return addOption(name, shortOption, longOption, description, numOfArgs, type, false);
    }

    /**
     * Adds a program argument description to be parsed.
     * 
     * @see <a
     *      href="http://commons.apache.org/proper/commons-cli/javadocs/api-release/org/apache/commons/cli/PatternOptionBuilder.html">PatternOptionBuilder</a>
     *      for supported type
     */
    public XAPConfigCLI addOption(String name, String shortOption, String longOption, String description, int numOfArgs, Class<?> type, boolean isMandatory) {
        options.addOption(builder(shortOption).argName(name).longOpt(longOption).desc(description).numberOfArgs(numOfArgs).type(type).required(isMandatory).build());
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
            if (ArrayUtils.isEmpty(args)) {
                printHelp();
            }
            CommandLineParser parser = new DefaultParser();
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
        formatter.setWidth(HELP_WIDTH);
        formatter.setOptionComparator((o1, o2) -> {
            String help = XapOption.HELP.getShortOption();
            return help.equals(o1.getOpt()) ? 1 : help.equals(o2.getOpt()) ? -1 : o1.getOpt().compareTo(o2.getOpt());
        });

        formatter.printHelp("XAP config options", options, true);
        System.exit(1);
    }
}
