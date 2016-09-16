package com.gigaspaces.gigapro.parser;

import com.gigaspaces.gigapro.xap_config_cli.XAPConfigCLI;
import com.gigaspaces.gigapro.xap_config_cli.XapOption;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static com.gigaspaces.gigapro.xap_config_cli.XapOption.*;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class GridInfoOptionsParser {

    private static final Set<XapOption> GRID_INFO_SERVICE_OPTIONS = EnumSet.of(HELP, LOOKUP_GROUPS, LOOKUP_LOCATORS, USERNAME, RMI_SERVER_HOSTNAME);

    private static final String GSA_COUNT_OPT_NAME = "gsa_count";
    private static final String GSA_COUNT_OPT = "c";
    private static final String GSA_COUNT_LONG_OPT = "gsa-count";
    private static final String GSA_COUNT_OPT_DESCRIPTION = "A count of GS Agents to be discovered";
    private static final int DEFAULT_GSA_COUNT = 1;

    private static final String WAIT_TIMEOUT_OPT_NAME = "wait_timeout";
    private static final String WAIT_TIMEOUT_OPT = "t";
    private static final String WAIT_TIMEOUT_LONG_OPT = "wait-timeout";
    private static final String WAIT_TIMEOUT_OPT_DESCRIPTION = "Wait timeout in seconds";
    private static final long DEFAULT_TIMEOUT = 10l;

    private static final Logger LOG = LoggerFactory.getLogger(GridInfoOptionsParser.class);

    public static GridInfoOptions parse(String[] args) throws ParseException {
        XAPConfigCLI xapConfigCLI = new XAPConfigCLI();
        xapConfigCLI.addOption(GSA_COUNT_OPT_NAME, GSA_COUNT_OPT, GSA_COUNT_LONG_OPT, GSA_COUNT_OPT_DESCRIPTION, 1, Number.class);
        xapConfigCLI.addOption(WAIT_TIMEOUT_OPT_NAME, WAIT_TIMEOUT_OPT, WAIT_TIMEOUT_LONG_OPT, WAIT_TIMEOUT_OPT_DESCRIPTION, 1, Number.class);
        xapConfigCLI.addXapOptions(GRID_INFO_SERVICE_OPTIONS);
        CommandLine cl = xapConfigCLI.parseArgs(args);

        GridInfoOptions options = new GridInfoOptions();
        options.count = getGsaCount(cl);
        options.lookupLocators = LOOKUP_LOCATORS.value(cl);
        options.lookupGroups = LOOKUP_GROUPS.value(cl);
        options.timeout = getWaitTimeout(cl);
        Optional<String> username = USERNAME.value(cl);
        options.username = username;
        if (username.isPresent()) {
            Console console = System.console();
            if (console == null) {
                throw new IllegalStateException("No console is associated with the current JVM. Exiting...");
            }
            options.password = Optional.<char[]> of(console.readPassword("Password for %s:", username.get()));
        } else {
            options.password = Optional.<char[]> empty();
        }
        options.rmiHostName = RMI_SERVER_HOSTNAME.value(cl);
        return options;
    }

    public static class GridInfoOptions {
        private int count;
        private long timeout;
        private Optional<String> lookupLocators;
        private Optional<String> lookupGroups;
        private Optional<String> username;
        private Optional<char[]> password;
        private Optional<String> rmiHostName;

        public int getCount() {
            return count;
        }

        public long getTimeout() {
            return timeout;
        }

        public Optional<String> getLookupLocators() {
            return lookupLocators;
        }

        public Optional<String> getLookupGroups() {
            return lookupGroups;
        }

        public Optional<String> getUsername() {
            return username;
        }

        public Optional<char[]> getPassword() {
            return password;
        }

        public Optional<String> getRmiHostName() {
            return rmiHostName;
        }
    }

    private static int getGsaCount(CommandLine cl) {
        if (!cl.hasOption(GSA_COUNT_OPT)) {
            LOG.info("No gsa count provided. The default gsa count = {} will be used", DEFAULT_GSA_COUNT);
            return DEFAULT_GSA_COUNT;
        }

        try {
            return Integer.parseInt(cl.getOptionValue(GSA_COUNT_OPT));
        } catch (NumberFormatException e) {
            LOG.warn("Invalid gsa count provided. The default gsa count = {} will be used", DEFAULT_GSA_COUNT, e);
            return DEFAULT_GSA_COUNT;
        }
    }

    private static long getWaitTimeout(CommandLine cl) {
        if (!cl.hasOption(WAIT_TIMEOUT_OPT)) {
            LOG.info("No wait timeout was provided. The default timeout = {} seconds will be used", DEFAULT_TIMEOUT);
            return DEFAULT_TIMEOUT;
        }
        try {
            return Long.parseLong(cl.getOptionValue(WAIT_TIMEOUT_OPT));
        } catch (NumberFormatException e) {
            LOG.warn("Invalid wait timeout was provided. The default timeout = {} seconds will be used.", DEFAULT_TIMEOUT, e);
            return DEFAULT_TIMEOUT;
        }
    }
}
