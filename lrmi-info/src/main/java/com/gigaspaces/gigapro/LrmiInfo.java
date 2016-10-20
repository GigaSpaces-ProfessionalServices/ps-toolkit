package com.gigaspaces.gigapro;

import com.gigaspaces.gigapro.xap_config_cli.XAPConfigCLI;
import com.gigaspaces.gigapro.xap_config_cli.XapOption;
import com.gigaspaces.lrmi.LRMIProxyMonitoringDetails;
import com.gigaspaces.lrmi.LRMIServiceMonitoringDetails;
import org.apache.commons.cli.CommandLine;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.transport.Transport;
import org.openspaces.admin.transport.TransportDetails;
import org.openspaces.admin.transport.TransportStatistics;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import static com.gigaspaces.gigapro.xap_config_cli.XapOptionUtils.getIntegerValue;
import static com.gigaspaces.gigapro.xap_config_cli.XapOptionUtils.getStringValue;
import static java.lang.System.out;
/**
 * @author Svitlana_Pogrebna
 *
 */
public class LrmiInfo {

    private static final Set<XapOption> XAP_OPTIONS = EnumSet.of(XapOption.LOOKUP_GROUPS, XapOption.LOOKUP_LOCATORS, XapOption.HELP); 
    private static final String GSC_COUNT_OPTION = "j";
    private static final String ITERATION_COUNT_OPTION = "z";
    private static final String LOG_TRANSPORT_INFO_OPTION = "t";
    
    public static void main(String[] args) throws Exception {
        // setup CLI and process args
        XAPConfigCLI xapConfigCLI = new XAPConfigCLI();
        xapConfigCLI.addXapOptions(XAP_OPTIONS);
        xapConfigCLI.addOption("GSC Count", GSC_COUNT_OPTION, "num-gscs", "Number of GSCs to wait for during Admin initialization", 1, Number.class, true);
        xapConfigCLI.addOption("Iteration Sleep", ITERATION_COUNT_OPTION, "iteration-sleep", "Millis to wait between iterations", 1, Number.class, true);
        xapConfigCLI.addOption("Log transport details", LOG_TRANSPORT_INFO_OPTION, "log-transport", "Whether to output transport details (true by default)", 1, Boolean.class, false);
        CommandLine xapCli = xapConfigCLI.parseArgs(args);
       
        Optional<String> groups = XapOption.LOOKUP_GROUPS.getValue(xapCli);
        Optional<String> locators = XapOption.LOOKUP_LOCATORS.getValue(xapCli);
        
        Optional<Integer> numGscs = getIntegerValue(GSC_COUNT_OPTION, xapCli);
        Optional<Integer> iterationSleep = getIntegerValue(ITERATION_COUNT_OPTION, xapCli);

        Optional<String> logTransportDetailsOption = getStringValue(LOG_TRANSPORT_INFO_OPTION, xapCli);
        boolean logTransportDetails = logTransportDetailsOption.isPresent() ? Boolean.parseBoolean(logTransportDetailsOption.get()) : true;
        
        // Create an admin and connect to the grid
        AdminFactory factory = new AdminFactory();
        if (groups.isPresent())
            factory.addGroups(groups.get());
        if (locators.isPresent())
            factory.addLocators(locators.get());
        
        Admin admin = factory.createAdmin();
        admin.getGridServiceContainers().waitFor(60, numGscs.get(), TimeUnit.SECONDS);

        CommaDelimitedWriter writer = new CommaDelimitedWriter();
        
        while (true) {
            
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
            
            Thread.sleep(iterationSleep.get());
            for (GridServiceContainer gsc : admin.getGridServiceContainers()) {
                out.println("~~~ GSC " + gsc.getUid());
                Date now = new Date(System.currentTimeMillis());
                out.println("~~~ current time " + format.format(now));
                out.println("~~~ machine " + gsc.getMachine().getHostAddress());
                out.println("~~~ PID " + gsc.getVirtualMachine().getDetails().getPid());
                out.println("~~~ started " + new Date(gsc.getVirtualMachine().getDetails().getStartTime()));
                
                JMXServiceURL jmxUrl = new JMXServiceURL(gsc.getVirtualMachine().getDetails().getJmxUrl());
                JMXConnector conn = JMXConnectorFactory.newJMXConnector(jmxUrl, null);
                conn.connect(null);
                MBeanServerConnection server = conn.getMBeanServerConnection();
                ObjectName on = new ObjectName("java.lang:type=Threading");
                Set<ObjectInstance> mbeans = server.queryMBeans(on, null);
                boolean outputTransportInfo = logTransportDetails;
                for (ObjectInstance mbean : mbeans) {
                    ObjectName name = mbean.getObjectName();
                    if (name.toString().contains("Threading")) {
                        long[] tids = (long[]) server.getAttribute(name, "AllThreadIds");
                        out.println("~~~ all threads сount " + tids.length);

                        outputTransportInfo &= outputThreadsInfo(tids, server, name, writer);
                    }
                }
                if (outputTransportInfo) {
                    outputTransportInfo(gsc, writer);
                }
            }
        }
    }
    
    private static boolean outputThreadsInfo(long[] tids, MBeanServerConnection server, ObjectName name, CommaDelimitedWriter writer) throws Exception {
        int lrmiThreadCount = 0;
        int lrmiSelectorReadThreadCount = 0;
        int lrmiSelectorWriteThreadCount = 0;
        int lrmiAsyncSelectorThreadCount = 0;
        int lrmiMonitoringThreadCount = 0;
        int lrmiLivenessThreadCount = 0;
        int lrmiCustomThreadCount = 0;
        int notifierThreadCount = 0;
        
        int leaseRenewalManagerThreadCount = 0;
        int backgroundFifoThreadThreadCount = 0;
        int processorpoolThreadCount = 0;
        int pendingAnswerspoolThreadCount = 0;
        int batchNotifierThreadCount = 0;
        int lookupDiscoveryTaskThreadCount = 0;
        int leaseManager$ReaperThreadCount = 0;
        int leaseRenewalManagerTaskThreadCount = 0;

        boolean fullResult = true;
        for (long tid : tids) {
            Long[] params = new Long[] { tid };
            String[] sigs = new String[] { "long" };
            CompositeDataSupport threadInfo = (CompositeDataSupport) server.invoke(name, "getThreadInfo", params, sigs);

            if (threadInfo == null){
                out.print("ERROR retrieving ThreadInfo: Is LRMI active?");
                fullResult = false;
                continue;
            }
            Object threadNameObj = threadInfo.get("threadName");
            String threadName = threadNameObj.toString();
            
            if (threadName.contains("LRMI Connection"))
                lrmiThreadCount++;
            else if (threadName.contains("LRMI-Selector-Read"))
                lrmiSelectorReadThreadCount++;
            else if (threadName.contains("LRMI-Selector-Write"))
                lrmiSelectorWriteThreadCount++;
            else if (threadName.contains("LRMI-async-Selector"))
                lrmiAsyncSelectorThreadCount++;
            else if (threadName.contains("LRMI Monitoring"))
                lrmiMonitoringThreadCount++;
            else if (threadName.contains("LeaseRenewalManager Task"))
                leaseRenewalManagerTaskThreadCount++;
            else if (threadName.contains("LeaseRenewalManager"))
                leaseRenewalManagerThreadCount++;
            else if (threadName.contains("BackgroundFifo"))
                backgroundFifoThreadThreadCount++;
            else if (threadName.contains("Processor-pool"))
                processorpoolThreadCount++;
            else if (threadName.contains("Pending Answers"))
                pendingAnswerspoolThreadCount++;
            else if (threadName.contains("Batch Notifier"))
                batchNotifierThreadCount++;
            else if (threadName.contains("LookupDiscovery Task"))
                lookupDiscoveryTaskThreadCount++;
            else if (threadName.contains("LeaseReaper"))
                leaseManager$ReaperThreadCount++;
            else if (threadName.contains("LRMI Custom Pool"))
                lrmiCustomThreadCount++;
            else if (threadName.contains("LRMI Liveness Pool")) 
                lrmiLivenessThreadCount++;
            else if (threadName.contains("Notifier-pool")) 
                notifierThreadCount++;
        }
        writer.write(LrmiThreadCount.CONNECTION, lrmiThreadCount);
        writer.write(LrmiThreadCount.SELECTOR_READ, lrmiSelectorReadThreadCount);
        writer.write(LrmiThreadCount.SELECTOR_WRITE, lrmiSelectorWriteThreadCount);
        writer.write(LrmiThreadCount.ASYNC_SELECTOR, lrmiAsyncSelectorThreadCount);
        writer.write(LrmiThreadCount.MONITORING, lrmiMonitoringThreadCount);
        writer.write(LrmiThreadCount.LIVENESS, lrmiLivenessThreadCount);
        writer.write(LrmiThreadCount.CUSTOM, lrmiCustomThreadCount);
        writer.write(LrmiThreadCount.NOTIFIER, notifierThreadCount);
        
        writer.write(LrmiThreadCount.LEASE_RENEWAL_MANAGER, leaseRenewalManagerThreadCount);
        writer.write(LrmiThreadCount.BACKGROUND_FIFO_THREAD, backgroundFifoThreadThreadCount);
        writer.write(LrmiThreadCount.PROCESSOR_POOL, processorpoolThreadCount);
        writer.write(LrmiThreadCount.PENDING_ANSWERS, pendingAnswerspoolThreadCount);
        writer.write(LrmiThreadCount.BATCH_NOTIFIER, batchNotifierThreadCount);
        writer.write(LrmiThreadCount.LOOKUP_DISCOVERY, lookupDiscoveryTaskThreadCount);
        writer.write(LrmiThreadCount.LEASE_MANAGER_REAPER, leaseManager$ReaperThreadCount);
        writer.write(LrmiThreadCount.LEASE_RENEWAL_MANAGER_TASK, leaseRenewalManagerTaskThreadCount);
        
        return fullResult;
    }
    
    private static class CommaDelimitedWriter {
        private boolean firstColumn = true;
        
        void write(Statistic stat, Number value) {
            Objects.requireNonNull(stat, "'stat' parameter must not be null");
            out.printf(firstColumn ? "%s,%s%n" : ",%s,%s%n", stat, value != null ? value : "NO VALUE");
            firstColumn = false;
        }

        void writeLast(Statistic stat, Number value) {
            write(stat, value);
            firstColumn = true;
        }
    }
    
    private static void outputTransportInfo(GridServiceContainer gsc, CommaDelimitedWriter writer) {

        Transport transportInfo = gsc.getTransport();
        transportInfo.getLRMIMonitoring().enableMonitoring();
        TransportStatistics stats = transportInfo.getStatistics();
        writer.write(LrmiTransport.QUEUE_SIZE, stats.getQueueSize());
        writer.write(LrmiTransport.ACTIVE_THREADS_COUNT, stats.getActiveThreadsCount());
        writer.write(LrmiTransport.ACTIVE_THREADS_PERC, stats.getActiveThreadsPerc());
        writer.write(LrmiTransport.COMPLETED_TASK_PER_SEC, stats.getCompletedTaskPerSecond());
        writer.write(LrmiTransport.COMPLETED_TASK_COUNT, stats.getCompletedTaskCount());
        TransportDetails transportDetails = stats.getDetails();
        Integer minThreads = transportDetails != null ? transportDetails.getMinThreads() : null;
        writer.write(LrmiTransport.LRMI_THREAD_POOL_MIN_THREAD_COUNT, minThreads);
        Integer maxThreads = transportDetails != null ? transportDetails.getMaxThreads() : null;
        writer.writeLast(LrmiTransport.LRMI_THREAD_POOL_MAX_THREAD_COUNT, maxThreads);
        LRMIServiceMonitoringDetails lrmiServiceDetails[] = transportInfo.getLRMIMonitoring().fetchMonitoringDetails().getInboundMonitoringDetails().getServicesMonitoringDetails();

        for (int i = 0; i < lrmiServiceDetails.length; i++) {
            out.println("XXX Inbound ServicesMonitoringDetails " + i + " :" + lrmiServiceDetails[i]);
        }

        LRMIProxyMonitoringDetails lrmiProxyDetails[] = transportInfo.getLRMIMonitoring().fetchMonitoringDetails().getOutboundMonitoringDetails().getProxiesMonitoringDetails();

        for (int i = 0; i < lrmiProxyDetails.length; i++) {
            out.println("XXX Outbound ProxiesMonitoringDetails " + i + " : " + lrmiProxyDetails[i]);
        }
    }
}
