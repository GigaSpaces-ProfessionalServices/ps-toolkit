package com.gigaspaces.gigapro.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

import static com.gigaspaces.gigapro.mail.SmtpPropertiesUtils.*;

/**
 * @author Svitlana_Pogrebna
 *
 */
public final class EmailSender {

    private EmailSender() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(EmailSender.class);

    public static void send(String attachFile) throws AddressException, MessagingException {
        if (attachFile == null || attachFile.isEmpty()) {
            throw new IllegalArgumentException("'attachFile' parameter must not be null or empty");
        }

        Properties properties = SmtpPropertiesUtils.getProperties();
        if (properties == null) {
            return;
        }

        String recipients = properties.getProperty(RECIPIENTS);
        if (recipients == null || recipients.isEmpty()) {
            return;
        }

        final String username = properties.getProperty(USERNAME);
        Authenticator auth = new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, properties.getProperty(PASSWORD));
            }
        };
        Session session = Session.getInstance(properties, auth);
        Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(username));

        msg.setRecipients(Message.RecipientType.TO, createAddresses(recipients));
        msg.setSubject(properties.getProperty(MSG_SUBJECT, ""));
        msg.setSentDate(new Date());

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(properties.getProperty(MSG_BODY, ""), "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        MimeBodyPart attachPart = new MimeBodyPart();
        try {
            attachPart.attachFile(attachFile);
        } catch (IOException ex) {
            LOG.warn("Failed to access file {}.", attachFile, ex);
            throw new MessagingException("Failed to access file " + attachFile, ex);
        }

        multipart.addBodyPart(attachPart);

        msg.setContent(multipart);

        Transport.send(msg);
    }

    private static Address[] createAddresses(String emailAddressesStr) throws AddressException {
        String[] emailAddresses = emailAddressesStr.split("\\s*,\\s*");

        Address[] addresses = new InternetAddress[emailAddresses.length];
        for (int i = 0; i < emailAddresses.length; i++) {
            addresses[i] = new InternetAddress(emailAddresses[i]);
        }
        return addresses;
    }
}
