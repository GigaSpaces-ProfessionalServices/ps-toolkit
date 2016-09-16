package com.gigaspaces.gigapro.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Svitlana_Pogrebna
 *
 */
public final class SmtpPropertiesUtils {

    private SmtpPropertiesUtils() {
    }

    private static final String PATH = "/mail.properties";
    private static final Logger LOG = LoggerFactory.getLogger(SmtpPropertiesUtils.class);

    public static final String USERNAME = "mail.smtp.user";
    public static final String PASSWORD = "mail.smtp.password";
    public static final String MSG_SUBJECT = "mail.msg.subject";
    public static final String MSG_BODY = "mail.msg.body";
    public static final String RECIPIENTS = "mail.msg.recipients";
    
    private static Properties properties;
    
    static {
        load();
    }
    
    private static void load() {
        InputStream inStream = SmtpPropertiesUtils.class.getResourceAsStream(PATH);

        if (inStream == null) {
            LOG.warn("Falied to find {}.", PATH);
            return;
        }
        try {
            properties = new Properties();
            properties.load(inStream);
        } catch (IOException e) {
            LOG.warn("Falied to load {}.", PATH);
        }
    }
    
    public static Properties getProperties() {
        return properties;
    }
}
