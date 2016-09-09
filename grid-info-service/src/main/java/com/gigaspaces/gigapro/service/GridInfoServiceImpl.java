package com.gigaspaces.gigapro.service;

import org.openspaces.admin.os.OperatingSystemDetails.VendorDetails;

import com.gigaspaces.cluster.replication.MirrorServiceConfig;
import com.gigaspaces.cluster.replication.sync.SyncReplPolicy;
import com.gigaspaces.gigapro.model.ClusterReplicationPolicy;
import com.gigaspaces.gigapro.model.CpuUsageInfo;
import com.gigaspaces.gigapro.model.GridInfo;
import com.gigaspaces.gigapro.parser.GridInfoOptionsParser.GridInfoOptions;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.cluster.ClusterPolicy;
import com.j_spaces.core.cluster.ReplicationPolicy;
import net.jini.core.discovery.LookupLocator;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.Machines;
import org.openspaces.admin.os.OperatingSystemDetails;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.vm.VirtualMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class GridInfoServiceImpl implements GridInfoService {

    private static final String COLON_SEPATATOR = ":";

    private static final Logger LOG = LoggerFactory.getLogger(GridInfoServiceImpl.class);
    
    private GridInfoOptions options;
    private Admin admin;
    
    public GridInfoServiceImpl(GridInfoOptions options) {
        this.options = options;
        initAdmin();
        waitForGsAgents();
    }
    
    private void initAdmin() {
        AdminFactory adminFactory = new AdminFactory();
        if (options.getLookupLocators().isPresent()) {
            adminFactory.addLocators(options.getLookupLocators().get());
        }
        if (options.getLookupGroups().isPresent()) {
            adminFactory.addGroups(options.getLookupGroups().get());
        }
        if (options.getUsername().isPresent() && options.getPassword().isPresent()) {
            char[] password = options.getPassword().get();
            adminFactory.credentials(options.getUsername().get(), String.valueOf(password));
            Arrays.fill(password, ' ');
        }
        
        if (options.getRmiHostName().isPresent()) {
            System.setProperty("java.rmi.server.hostname", options.getRmiHostName().get());
        }
        admin = adminFactory.discoverUnmanagedSpaces().createAdmin();
        admin.setDefaultTimeout(options.getTimeout(), TimeUnit.SECONDS);
    }
   
    private void waitForGsAgents() {
        if (!admin.getGridServiceAgents().waitFor(options.getCount(), options.getTimeout(), TimeUnit.SECONDS)) {
            throw new IllegalStateException(String.format("%d GSA(s) have not been found during %d seconds", options.getCount(), options.getTimeout()));
        }
    }
    
    @Override
    public GridInfo collectGridInfo() {
        GridInfo gridInfo = new GridInfo();
        
        gridInfo.setLookupGroups(new HashSet<String>(Arrays.<String> asList(admin.getGroups())));
        for (LookupLocator ll : admin.getLocators()) {
            gridInfo.getLookupLocators().add(ll.getHost() + COLON_SEPATATOR + ll.getPort());
        }

        for (VirtualMachine virtualMachine : admin.getVirtualMachines()) {
            gridInfo.getIpAddresses().add(virtualMachine.getMachine().getHostAddress());
        }

        long waitTimeout = options.getTimeout();
        for (GridServiceAgent gsa : admin.getGridServiceAgents()) {
            gsa.getGridServiceManagers().waitForAtLeastOne(waitTimeout, TimeUnit.SECONDS);
            for (GridServiceManager gsm : gsa.getGridServiceManagers()) {
                String hostAddress = gsm.getMachine().getHostAddress();
                gridInfo.getGsmAddresses().add(hostAddress);
            }
            gsa.getLookupServices().waitFor(1, waitTimeout, TimeUnit.SECONDS);
            for (LookupService ls : gsa.getLookupServices()) {
                String hostAddress = ls.getMachine().getHostAddress();
                gridInfo.getLusAddresses().add(hostAddress);
            }
            gsa.getGridServiceContainers().waitFor(1, waitTimeout, TimeUnit.SECONDS);
            for (GridServiceContainer gsc : gsa.getGridServiceContainers()) {
                Map<String, Integer> gscAddresses = gridInfo.getGscAddresses();
                String hostAddress = gsc.getMachine().getHostAddress();
                Integer count = gscAddresses.getOrDefault(hostAddress, 0);
                gscAddresses.put(hostAddress, ++count);
            }
        }

        gridInfo.setSpaces(admin.getSpaces().getNames().keySet());

        for (Space space : admin.getSpaces()) {
            IJSpace spaceProxy = space.getGigaSpace().getSpace();
            if (spaceProxy.isSecured()) {
                gridInfo.getSecuredSpaces().add(space.getName());
            }
        }
        return gridInfo;
    }

    @Override
    public Map<String, ClusterReplicationPolicy> collectClusterReplicationPolicyInfo() {
        Map<String, ClusterReplicationPolicy> replPolicyMap = new HashMap<>();
        for (Space space : admin.getSpaces()) {
            try {
                IJSpace spaceProxy = space.getGigaSpace().getSpace();
                IRemoteJSpaceAdmin spaceAdmin = (IRemoteJSpaceAdmin) (spaceProxy.getAdmin());

                ClusterPolicy clusterPolicy = spaceAdmin.getClusterPolicy();
                if (clusterPolicy == null) { // e.g. for 'mirror' space
                    continue; 
                }
                ReplicationPolicy replicationPolicy = clusterPolicy.getReplicationPolicy();

                ClusterReplicationPolicy clusterReplicationPolicy = replPolicyMap.get(space.getName());
                if (clusterReplicationPolicy == null && replicationPolicy != null) {
                    clusterReplicationPolicy = new ClusterReplicationPolicy();
                    replPolicyMap.put(space.getName(), clusterReplicationPolicy);
                    clusterReplicationPolicy.setReplicationMode(replicationPolicy.m_ReplicationMode);
                    clusterReplicationPolicy.setPolicyType(replicationPolicy.m_PolicyType == 0 ? "full-replication" : "partial-replication");
                    clusterReplicationPolicy.setReplFindTimeout(replicationPolicy.m_SpaceFinderTimeout);
                    clusterReplicationPolicy.setReplFullTake(replicationPolicy.isReplicateFullTake());
                    clusterReplicationPolicy.setReplNotifyTemplate(replicationPolicy.m_ReplicateNotifyTemplates);
                    clusterReplicationPolicy.setTriggerNotifyTemplate(replicationPolicy.m_TriggerNotifyTemplates);
                    clusterReplicationPolicy.setReplChunkSize(replicationPolicy.m_ReplicationChunkSize);
                    clusterReplicationPolicy.setReplIntervalMillis(replicationPolicy.m_ReplicationIntervalMillis);
                    clusterReplicationPolicy.setReplIntervalOpers(replicationPolicy.m_ReplicationIntervalOperations);
                    clusterReplicationPolicy.setAsyncChannelShutdownTimeout(replicationPolicy.getAsyncChannelShutdownTimeout());
                    clusterReplicationPolicy.setOnConflictingPackates(replicationPolicy.getConflictingOperationPolicy() != null ? replicationPolicy.getConflictingOperationPolicy().name().toLowerCase() : "");

                    SyncReplPolicy syncReplPolicy = replicationPolicy.m_SyncReplPolicy;
                    if (syncReplPolicy != null) {
                        clusterReplicationPolicy.setThrottleWhenInactive(syncReplPolicy.isThrottleWhenInactive());
                        clusterReplicationPolicy.setMaxThrottleTpWhenInactive(syncReplPolicy.getMaxThrottleTPWhenInactive());
                        clusterReplicationPolicy.setMinThrottleTpWhenInactive(syncReplPolicy.getMinThrottleTPWhenActive());
                        clusterReplicationPolicy.setMultipleOpersChunkSize(syncReplPolicy.getMultipleOperationChunkSize());
                        clusterReplicationPolicy.setTargetConsumeTimeout(syncReplPolicy.getTargetConsumeTimeout());
                    }
                    MirrorServiceConfig mirrorServiceConfig = replicationPolicy.getMirrorServiceConfig();
                    if (mirrorServiceConfig != null) {
                        clusterReplicationPolicy.setMirrorName(mirrorServiceConfig.memberName);
                        clusterReplicationPolicy.setMirrorUrl(mirrorServiceConfig.serviceURL != null ? mirrorServiceConfig.serviceURL.getURL() : "");
                        clusterReplicationPolicy.setMirrorBulkSize(mirrorServiceConfig.bulkSize);
                        clusterReplicationPolicy.setMirrorIntervalMillis(mirrorServiceConfig.intervalMillis);
                        clusterReplicationPolicy.setMirrorIntervalOpers(mirrorServiceConfig.intervalOpers);
                        clusterReplicationPolicy.setOnRedoLogCapacityExceeded(mirrorServiceConfig.onRedoLogCapacityExceeded != null ? mirrorServiceConfig.onRedoLogCapacityExceeded.name().toLowerCase() : "");
                        clusterReplicationPolicy.setRedoLogCapacity(mirrorServiceConfig.maxRedoLogCapacity);
                    }
                }
            } catch (RemoteException e) {
                LOG.error("Failed to get space proxy admin for space {}", space.getName(), e);
                throw new IllegalStateException("Failed to get space proxy admin for space" + space.getName(), e);
            }
        }
        return replPolicyMap;
    }
    
    @Override
    public Set<CpuUsageInfo> collectCpuUsageInfo() {
        long timeout = options.getTimeout();
        if (!admin.getLookupServices().waitFor(1, timeout, TimeUnit.SECONDS)) {
            throw new IllegalStateException("No Lookup Services have not been found during " + timeout + " seconds");
        }
        Machines machines = admin.getMachines();
        if (!machines.waitFor(1, timeout, TimeUnit.SECONDS)) {
            throw new IllegalStateException("No machines have not been found during " + timeout + " seconds");
        }
        Set<CpuUsageInfo> cpuUsageInfoSet = new HashSet<>();
        for (Machine machine : machines) {
            CpuUsageInfo cpuUsageInfo = new CpuUsageInfo();
            cpuUsageInfo.setHostName(machine.getHostName());
            cpuUsageInfo.setHostAddress(machine.getHostAddress());
            OperatingSystemDetails os = machine.getOperatingSystem().getDetails();
            cpuUsageInfo.setAvailableProcessors(os.getAvailableProcessors());
            cpuUsageInfo.setOperationSystem(os.getName() + " " + os.getVersion() + " " + os.getArch());
            double totalPhysicalMemorySizeInMB = os.getTotalPhysicalMemorySizeInMB();
            cpuUsageInfo.setPhysicalMemorySizeInMB(totalPhysicalMemorySizeInMB);
            cpuUsageInfoSet.add(cpuUsageInfo);
        }
        return cpuUsageInfoSet;
    }

    @Override
    public void close() {
        if (admin != null) {
            admin.close();
        }
    }
}
