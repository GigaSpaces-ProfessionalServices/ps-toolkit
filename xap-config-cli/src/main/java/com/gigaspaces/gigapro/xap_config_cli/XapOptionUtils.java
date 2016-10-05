package com.gigaspaces.gigapro.xap_config_cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Svitlana_Pogrebna
 *
 */
public final class XapOptionUtils {

    private XapOptionUtils() {
    }

    /**
     * Returns option value converted to an option type.
     * 
     * @see <a
     *      href="http://commons.apache.org/proper/commons-cli/javadocs/api-release/org/apache/commons/cli/PatternOptionBuilder.html">PatternOptionBuilder</a>
     *      for supported type
     * 
     * @param shortName
     * @param commandLine
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getParsedValue(String shortName, CommandLine commandLine) {
        checkForNull(shortName, commandLine);
        try {
            return Optional.ofNullable((T) commandLine.getParsedOptionValue(shortName));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    private static void checkForNull(String shortName, CommandLine commandLine) {
        Objects.requireNonNull(shortName, "'shortName' must not be null");
        Objects.requireNonNull(commandLine, "'commandLine' must not be null");
    }
    
    /**
     * Returns option value
     * 
     * @param shortName
     * @param commandLine
     * @return
     */
    public static Optional<String> getStringValue(String shortName, CommandLine commandLine) {
        checkForNull(shortName, commandLine);
        if (!commandLine.hasOption(shortName)) {
            return Optional.empty();
        }
        return Optional.of(commandLine.getOptionValue(shortName));
    }
    
    /**
     * Returns option integer value.
     * Note: the option should have java.lang.Number type.
     *  
     * @see <a
     *      href="http://commons.apache.org/proper/commons-cli/javadocs/api-release/org/apache/commons/cli/PatternOptionBuilder.html">PatternOptionBuilder</a>
     *      for supported type
     *      
     * @param shortName
     * @param commandLine
     * @return
     */
    public static Optional<Integer> getIntegerValue(String shortName, CommandLine commandLine) {
        Optional<Number> numericValue = getParsedValue(shortName, commandLine);
        return numericValue.isPresent() ? Optional.of(numericValue.get().intValue()) : Optional.empty();
    }
}

