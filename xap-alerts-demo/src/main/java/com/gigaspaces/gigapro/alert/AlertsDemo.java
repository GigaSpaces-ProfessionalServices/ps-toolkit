package com.gigaspaces.gigapro.alert;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.alert.config.parser.XmlAlertConfigurationParser;
import org.openspaces.admin.alert.AlertManager;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.Admin;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class AlertsDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertsDemo.class); 
    
    private static final String ALERTS_CONFIG = "alerts.xml";
    private static final int PARAM_COUNT = 2;

    public static void main(String[] args) {
        if (args.length < PARAM_COUNT) {
            System.out.println("Invalid arguments count. Usage: java -jar xap-alerts-demo.jar \"lookupgroup\" \"lookuplocators\"");
            System.exit(1);
        }
        String group = args[0];
        String locator = args[1];

        Admin admin = new AdminFactory().addGroup(group).addLocator(locator).createAdmin();

        AlertManager alertManager = admin.getAlertManager();

        alertManager.configure(new XmlAlertConfigurationParser(ALERTS_CONFIG).parse());

        alertManager.getAlertTriggered().add(new AlertTriggeredEventListener() {

            @Override
            public void alertTriggered(Alert alert) {
                LOGGER.info(alert.toString());
            }
        });
    }
}
