package com.gigaspaces.gigapro.rebalancer;

import com.beust.jcommander.JCommander;
import com.gigaspaces.gigapro.rebalancer.config.Configuration;
import com.google.common.base.Joiner;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;

import java.io.Console;
import java.util.List;
import java.util.logging.Logger;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Created by Skyler on 3/17/2016.
 */
public class Program {
    private Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    public static void main(String[] args) {
        Program p = new Program();
        p.run(parseArguments(args));
    }

    private void run(Configuration configuration) {
        logger.info("Initializing rebalancer with the following options: " + configuration.toString());
        AdminFactory adminFactory = new AdminFactory();

        if (isNotEmpty(configuration.getLocators()))
            adminFactory.addLocators(join(configuration.getLocators()));

        if (isNotEmpty(configuration.getGroups()))
            adminFactory.addGroups(join(configuration.getGroups()));

        Console console = System.console();
        if (configuration.isSecured()) {
            String username = console.readLine("XAP Username: ");
            String password = new String(console.readPassword("XAP Password: "));

            adminFactory.credentials(username, password);
        }

        Admin admin = adminFactory.create();

        ProcessingUnitBalancer balancer = new ProcessingUnitBalancer(admin, configuration);

        try {
            balancer.balance();
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            ex.printStackTrace();
        } finally {
            admin.close();
        }
    }

    private static Configuration parseArguments(String[] args) {
        Configuration output = new Configuration();
        JCommander jCommander = new JCommander(output);
        jCommander.parse(args);
        return output;
    }

    private String join(List<String> args) {
        return Joiner.on(",").join(args);
    }
}
