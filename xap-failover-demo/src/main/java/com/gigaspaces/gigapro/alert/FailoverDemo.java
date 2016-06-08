package com.gigaspaces.gigapro.alert;

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
public class FailoverDemo {

    private static final int PARAM_COUNT = 2;

    public static void main(String[] args) {
        if (args.length != PARAM_COUNT) {
            System.out.println("Invalid arguments count. Usage java [options] com.gigaspaces.gigapro.alert.FailoverDemo \"lookupgroup\" \"lookuplocators\"");
            System.exit(1);
        }
        String group = args[0];
        String locator = args[1];

        Admin admin = new AdminFactory().addGroup(group).addLocator(locator).createAdmin();

        AlertManager alertManager = admin.getAlertManager();

        alertManager.configure(new XmlAlertConfigurationParser("alerts.xml").parse());

        alertManager.getAlertTriggered().add(new AlertTriggeredEventListener() {

            @Override
            public void alertTriggered(Alert alert) {
                System.out.println(alert);
            }
        });
    }
}
