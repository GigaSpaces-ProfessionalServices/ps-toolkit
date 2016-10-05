package com.gigaspaces.gigapro;

import com.gigaspaces.gigapro.xap_config_cli.*;
import com.gigaspaces.lrmi.LRMIProxyMonitoringDetails;
import com.gigaspaces.lrmi.LRMIServiceMonitoringDetails;
import org.apache.commons.cli.CommandLine;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.transport.Transport;

import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class LrmiInfo {

    public static void main(String[] args) throws Exception {

        // setup CLI and process args
        XAPConfigCLI xapConfigCLI = new XAPConfigCLI();
        xapConfigCLI.addXapOptions(EnumSet.allOf(XapOption.class));
        xapConfigCLI.addOption("GSC Count", "j", "num-gscs", "Number of GSCs to wait for during Admin initialization", 1, Integer.class);
        xapConfigCLI.addOption("Iteration Sleep", "q", "iteration-sleep", "Millis to wait between iterations", 5000, Integer.class);
        CommandLine xapCli = xapConfigCLI.parseArgs(args);
        String groups = xapCli.getOptionValue(XapOption.LOOKUP_GROUPS.getShortOption());
        String locators = xapCli.getOptionValue(XapOption.LOOKUP_GROUPS.getShortOption());
        Integer numGscs = Integer.valueOf(xapCli.getOptionValue("j"));
        Integer iterationSleep = Integer.valueOf(xapCli.getOptionValue("q"));

        // Create an admin and connect to the grid
        AdminFactory factory = new AdminFactory();
        if( groups != null )
            factory.addGroups(groups);
        if( locators != null )
            factory.addLocators(locators);
        Admin admin = factory.createAdmin();
        admin.getGridServiceContainers().waitFor(60, numGscs, TimeUnit.SECONDS);

        while (true) {
            Thread.sleep(iterationSleep);
            for (GridServiceContainer gsc : admin.getGridServiceContainers()) {
                System.out.println("--------- GSC [" + gsc.getUid() + "] running on Machine " + gsc.getMachine().getHostAddress() + " Pid:" + gsc.getVirtualMachine().getDetails().getPid() + " Start Time:" + new Date(gsc.getVirtualMachine().getDetails().getStartTime()) + " --------- ");

                JMXServiceURL jmxUrl = new JMXServiceURL(gsc.getVirtualMachine().getDetails().getJmxUrl());
                JMXConnector conn = JMXConnectorFactory.newJMXConnector(jmxUrl, null);
                conn.connect(null);
                MBeanServerConnection server = conn.getMBeanServerConnection();
                ObjectName on = new ObjectName("java.lang:type=Threading");
                Set<ObjectInstance> mbeans = server.queryMBeans(on, null);
                for (ObjectInstance mbean : mbeans) {
                    ObjectName name = mbean.getObjectName();
                    if (name.toString().contains("Threading")) {
                        long[] tids = (long[]) server.getAttribute(name, "AllThreadIds");
                        System.out.println("All Thread Count:" + tids.length);

                        int lrmiThreadCount = 0;
                        int lrmiSelectorReadThreadCount = 0;
                        int lrmiSelectorWriteThreadCount = 0;
                        int lrmiAsyncSelectorThreadCount = 0;
                        int lrmiMonitoringThreadCount = 0;
                        int lrmiLivenessThreadCount = 0;

                        int leaseRenewalManagerThreadCount = 0;
                        int backgroundFifoThreadThreadCount = 0;
                        int processorpoolThreadCount = 0;
                        int pendingAnswerspoolThreadCount = 0;
                        int batchNotifierThreadCount = 0;
                        int lookupDiscoveryTaskThreadCount = 0;
                        int leaseManager$ReaperThreadCount = 0;
                        int leaseRenewalManagerTaskThreadCount = 0;

                        for (long tid : tids) {
                            Long[] params = new Long[] { tid };
                            String[] sigs = new String[] { "long" };
                            CompositeDataSupport threadInfo;
                            threadInfo = (CompositeDataSupport) server.invoke(name, "getThreadInfo", params, sigs);
                            Object threadName = threadInfo.get("threadName");
                            if (threadName.toString().indexOf("LRMI Connection") > -1)
                                lrmiThreadCount++;
                            if (threadName.toString().indexOf("LRMI-Selector-Read") > -1)
                                lrmiSelectorReadThreadCount++;
                            if (threadName.toString().indexOf("LRMI-Selector-Write") > -1)
                                lrmiSelectorWriteThreadCount++;
                            if (threadName.toString().indexOf("LRMI-async-Selector") > -1)
                                lrmiAsyncSelectorThreadCount++;
                            if (threadName.toString().indexOf("LRMI Monitoring") > -1)
                                lrmiMonitoringThreadCount++;
                            if (threadName.toString().indexOf("LeaseRenewalManager") > -1)
                                leaseRenewalManagerThreadCount++;
                            if (threadName.toString().indexOf("BackgroundFifo") > -1)
                                backgroundFifoThreadThreadCount++;
                            if (threadName.toString().indexOf("Processor-pool") > -1)
                                processorpoolThreadCount++;
                            if (threadName.toString().indexOf("Pending Answers") > -1)
                                pendingAnswerspoolThreadCount++;
                            if (threadName.toString().indexOf("Batch Notifier") > -1)
                                batchNotifierThreadCount++;
                            if (threadName.toString().indexOf("LookupDiscovery Task") > -1)
                                lookupDiscoveryTaskThreadCount++;
                            if (threadName.toString().indexOf("LeaseManager$Reaper") > -1)
                                leaseManager$ReaperThreadCount++;
                            if (threadName.toString().indexOf("LeaseRenewalManager Task") > -1)
                                leaseRenewalManagerTaskThreadCount++;
                        }
                        System.out.println("LRMI Connection Thread Count:" + lrmiThreadCount);
                        System.out.println("LRMI Selector-Read Thread Count:" + lrmiSelectorReadThreadCount);
                        System.out.println("LRMI Selector-Write Thread Count:" + lrmiSelectorWriteThreadCount);
                        System.out.println("LRMI async-Selector Thread Count:" + lrmiAsyncSelectorThreadCount);
                        System.out.println("LRMI Monitoring Thread Count:" + lrmiMonitoringThreadCount);
                        System.out.println("LRMI Liveness Thread Count:" + lrmiLivenessThreadCount);

                        System.out.println("Lease Renewal Manager Thread Count:" + leaseRenewalManagerThreadCount);
                        System.out.println("Background Fifo Thread Count:" + backgroundFifoThreadThreadCount);
                        System.out.println("Processor pool Thread Count:" + processorpoolThreadCount);
                        System.out.println("Pending Answers pool Thread Count:" + pendingAnswerspoolThreadCount);
                        System.out.println("Batch Notifier Thread Count:" + batchNotifierThreadCount);
                        System.out.println("Lookup Discovery Task Thread Count:" + lookupDiscoveryTaskThreadCount);
                        System.out.println("Lease Manager Reaper Thread Count:" + leaseManager$ReaperThreadCount);
                        System.out.println("Lease Renewal Manager Task Thread Count:" + leaseRenewalManagerTaskThreadCount);

                    }
                }
                Transport transportInfo = gsc.getTransport();
                transportInfo.getLRMIMonitoring().enableMonitoring();
                System.out.println("LRMI Transport Statistics - Active Threads Count:" + transportInfo.getStatistics().getActiveThreadsCount());
                System.out.println("LRMI Transport Statistics - Queue Size:" + transportInfo.getStatistics().getQueueSize());
                System.out.println("LRMI Transport Statistics - Active Threads Perc:" + transportInfo.getStatistics().getActiveThreadsPerc());
                System.out.println("LRMI Transport Statistics - Completed Task Count:" + transportInfo.getStatistics().getCompletedTaskCount());
                System.out.println("LRMI Transport Statistics - Completed Task Per Second:" + transportInfo.getStatistics().getCompletedTaskPerSecond());

                LRMIServiceMonitoringDetails lrmiServiceDetails[] = transportInfo.getLRMIMonitoring().fetchMonitoringDetails().getInboundMonitoringDetails().getServicesMonitoringDetails();

                for (int i = 0; i < lrmiServiceDetails.length; i++) {
                    System.out.println("Inbound ServicesMonitoringDetails " + i + " :" + lrmiServiceDetails[i]);
                }

                LRMIProxyMonitoringDetails lrmiProxyDetails[] = transportInfo.getLRMIMonitoring().fetchMonitoringDetails().getOutboundMonitoringDetails().getProxiesMonitoringDetails();

                for (int i = 0; i < lrmiProxyDetails.length; i++) {
                    System.out.println("Outbound ProxiesMonitoringDetails " + i + " : " + lrmiProxyDetails[i]);
                }
            }
        }
    }
}
