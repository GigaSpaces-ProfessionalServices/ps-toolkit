package com.gigaspaces.gigapro.zombie_finder;

import com.gigaspaces.grid.gsa.AgentProcessDetails;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ZombieFinder {
    private Properties properties = new Properties();
     /*
     private String lookupLocators;
     private String host = "192.168.1.1";
     private int gsaCount;
     private long timeout;
     private Set<String> hostSet = new HashSet<String>();
     private List<XapProcess> hostProcessList = new ArrayList<XapProcess>();
     private List<XapProcess> adminProcessList = new ArrayList<XapProcess>();

     private void parseCommandLine(String[] args) {
        System.out.println("Begin parsing command line parameters.");

        lookupLocators = args[0];
        host = args[1];
        gsaCount = Integer.parseInt(args[2]);
        timeout = Long.parseLong(args[3]);

        System.out.println("Completed parsing command line parameters.");
    }
     */

    public static void main(String[] args) {
        System.out.println("ZombieFinder started.");

        ZombieFinder zf = new ZombieFinder();
        // zf.parseCommandLine(args);
        zf.loadProperties(args);
        zf.checkForZombies();

        System.out.println("ZombieFinder completed.");
        System.exit(0);
    }

    private void loadProperties(String[] args) {
        System.out.println("Begin loading properties file.");

        try {
            properties.load(getClass().getResourceAsStream("zombie-finder-" + args[0] + ".properties"));
            /*
            Enumeration<?> keys = properties.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = (String) properties.get(key);
                System.out.println(key + ": " + value);
            }
            */
        } catch (Exception exception) {
            System.out.println("EXCEPTION: " + exception.getMessage());
            System.exit(1);
        }

        System.out.println("Completed loading properties file.");
    }

    private String getLookupLocator() {
        return properties.getProperty("lookup-locator", "");
    }

    private Set<String> getConfiguredHosts() {
        System.out.println("Begin getting hosts from configuration.");

        Set<String> hostSet = new HashSet<>();
        properties.stringPropertyNames().stream()
                .filter(propertyName -> propertyName.startsWith("host"))
                .forEach(propertyName -> {
                    String[] tokens = propertyName.trim().split("\\|");
                    hostSet.add(tokens[1]);
                });

        System.out.println("Completed getting hosts from configuration.");
        return hostSet;
    }

    private int getTotalGSAs() {
        int count = 0;
        for (String host : getConfiguredHosts()) {
            count += Integer.valueOf(properties.getProperty("host|" + host + "|total-gsa", "0"));
        }
        return count;
    }

    private int getTotalGSCs() {
        int count = 0;
        for (String host : getConfiguredHosts()) {
            count += Integer.valueOf(properties.getProperty("host|" + host + "|total-gsc", "0"));
        }
        return count;
    }

    private long getTimeout() {
        return Long.valueOf(properties.getProperty("timeout", "20000"));
    }

    private XapProcess buildXapProcess(String host, String processId, String serviceType) {
        return new XapProcess(host, processId, serviceType);
    }

    private List<XapProcess> getHostProcesses() {
        System.out.println("Begin getting host processes.");

        List<XapProcess> hostProcessList = new ArrayList<>();
        for (String host : getConfiguredHosts()) {
            hostProcessList.addAll(findXapProcesses(host));
        }
        if (hostProcessList.size() != getTotalGSCs()) {
            System.out.println("EXCEPTION: Was not able to locate configured number of GSCs.");
            System.exit(1);
        }

        System.out.println("Completed getting host processes.");
        return hostProcessList;
    }

    private List<XapProcess> findXapProcesses(String host) {
        List<XapProcess> hostProcessList = new ArrayList<>();
        BufferedReader reader = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("/bin/sh", "-c", "ps -ef | grep 'com.gigaspaces.start.SystemBoot'");
            Process process = processBuilder.start();
            if (process.waitFor() != 0) {
                System.out.println("EXCEPTION: There was a problem running the remote server process.");
                System.exit(1);
            }
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output;
            while ((output = reader.readLine()) != null) {
                // System.out.println("Found a PID: " + output);
                if (output.contains("services=GSC")) {
                    String[] tokens = output.trim().split(" +");
                    hostProcessList.add(buildXapProcess(host, tokens[1], "gsc"));
                }
            }
        } catch (Exception exception) {
            System.out.println("EXCEPTION: " + exception.getMessage());
            System.exit(1);
        } finally {
            try {
                assert reader != null;
                reader.close();
            } catch (Exception exception) {
                System.out.println("EXCEPTION: " + exception.getMessage());
                System.exit(1);
            }
        }
        return hostProcessList;
    }

    private List<XapProcess> getAdminProcesses() {
        System.out.println("Begin getting XAP Admin processes.");

        Admin admin = new AdminFactory().addLocator(getLookupLocator()).createAdmin();
        admin.getGridServiceAgents().waitFor(getTotalGSAs(), getTimeout(), TimeUnit.MILLISECONDS);
        if (admin.getGridServiceAgents().getSize() != getTotalGSAs()) {
            System.out.println("EXCEPTION: Was not able to locate configured number of GSAs.");
            System.exit(1);
        }
        List<XapProcess> adminProcessList = new ArrayList<>();
        for (GridServiceAgent gsa : admin.getGridServiceAgents()) {
            //System.out.println("GSA [" + gsa.getUid() + "] running on Machine " + gsa.getMachine().getHostName() + "|" + gsa.getMachine().getHostAddress());
            for (AgentProcessDetails agentProcessDetails : gsa.getProcessesDetails()) {
                //System.out.println(" PID:" + agentProcessDetails.getProcessId() + " -> Process [" + Arrays.toString(agentProcessDetails.getCommand()) + "]");
                if ("gsc".equals(agentProcessDetails.getServiceType())) {
                    adminProcessList.add(buildXapProcess(gsa.getMachine().getHostAddress(),
                            ((Long) agentProcessDetails.getProcessId()).toString(),
                            agentProcessDetails.getServiceType()));
                }
            }
        }

        System.out.println("Completed getting XAP Admin processes.");
        return adminProcessList;
    }

    private void checkForZombies() {
        System.out.println("Begin checking for zombie XAP processes.");

        List<XapProcess> hostProcessList = getHostProcesses();
        List<XapProcess> adminProcessList = getAdminProcesses();
        int totalZombies = 0;
        for (XapProcess xapProcess : hostProcessList) {
            if (!adminProcessList.contains(xapProcess)) {
                System.out.println("Found a zombie XapProcess: [Host: " + xapProcess.getHost() + ", ProcessId: " + xapProcess.getProcessId() + "]");
                totalZombies++;
            }
        }
        System.out.println("Found total of " + totalZombies + " zombie findXapProcesses(es).");

        System.out.println("Completed checking for zombie XAP processes.");
    }
}
