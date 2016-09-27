package com.gigaspaces.gigapro.inspector.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNumeric;

/**
 * @author Svitlana_Pogrebna
 *
 */
public final class Configuration {

    private static final String LOG_FREQUENCY_KEY = "LOG_FREQUENCY";
    private static final int DEFAULT_LOG_FREQUENCY = 10;

    private static final String HEAD_SIZE_KEY = "HEAD_SIZE";
    private static final int DEFAULT_HEAD_SIZE = 1024;

    private static final String TAIL_SIZE_KEY = "TAIL_SIZE";
    private static final int DEFAULT_TAIL_SIZE = 1024;
    
    private static final String DATA_SET_KEY = "DATASET_SIZE";
    private static final int DEFAULT_DATA_SET_KEY = 1024;
    
    private Configuration() {
    }
    
    private static final Logger LOG = LoggerFactory.getLogger("ps-inspector");
    
    public static int getLogFrequency() {
        return parseIntProperty(getProperty(LOG_FREQUENCY_KEY), LOG_FREQUENCY_KEY, DEFAULT_LOG_FREQUENCY);
    }
    
    public static int getHeadSize() {
        return parseIntProperty(getProperty(HEAD_SIZE_KEY), HEAD_SIZE_KEY, DEFAULT_HEAD_SIZE);
    }
    
    public static int getTallSize() {
        return parseIntProperty(getProperty(TAIL_SIZE_KEY), TAIL_SIZE_KEY, DEFAULT_TAIL_SIZE);
    }
    
    public static int getDataSetSize() {
        return parseIntProperty(getProperty(DATA_SET_KEY), DATA_SET_KEY, DEFAULT_DATA_SET_KEY);
    }
    
    private static int parseIntProperty(String propertyValue, String propertyName, int defaultValue) {
        if (isBlank(propertyValue)) {
            LOG.trace(format("Property %s is not set! Default value [%d] will be set.", propertyName, defaultValue));
            return defaultValue;
        } else if (!isNumeric(propertyValue)) {
            LOG.trace(format("Property %s [%s] is not a number! Default value [%d] will be set.", propertyName, propertyValue, defaultValue));
            return defaultValue;
        } else {
            int result = parseInt(propertyValue);
            LOG.trace(format("%s is set to [%s].", propertyName, propertyValue));
            return result;
        }
    }

}
