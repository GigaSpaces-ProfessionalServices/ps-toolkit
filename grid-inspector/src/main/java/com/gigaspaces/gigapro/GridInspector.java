package com.gigaspaces.gigapro;

import com.gigaspaces.cluster.replication.MirrorServiceConfig;

import org.openspaces.admin.space.Spaces;
import com.gigaspaces.cluster.replication.sync.SyncReplPolicy;
import com.gigaspaces.gigapro.convert.Converter;
import com.gigaspaces.gigapro.convert.PropertiesFormatConverter;
import com.gigaspaces.gigapro.model.ClusterReplicationPolicy;
import com.gigaspaces.gigapro.model.GridInfo;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.cluster.ReplicationPolicy;
import net.jini.core.discovery.LookupLocator;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.vm.VirtualMachine;

import java.rmi.RemoteException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class GridInspector {

    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_USER_PASSWORD = "admin";

    private static final int PARAM_COUNT = 1;
    private static final long TIMEOUT = 10l;
    private static final String COLON_SEPATATOR = ":";

    private final Converter converter = new PropertiesFormatConverter();

    private GridInfo createGridInfo(Admin admin) {
        GridInfo gridInfo = new GridInfo();

        gridInfo.setLookupGroups(new HashSet<String>(Arrays.<String> asList(admin.getGroups())));
        for (LookupLocator ll : admin.getLocators()) {
            gridInfo.getLookupLocators().add(ll.getHost() + COLON_SEPATATOR + ll.getPort());
        }

        for (VirtualMachine virtualMachine : admin.getVirtualMachines()) {
            gridInfo.getIpAddresses().add(virtualMachine.getMachine().getHostAddress());
        }

        for (GridServiceAgent gsa : admin.getGridServiceAgents()) {
            gsa.getGridServiceManagers().waitForAtLeastOne(TIMEOUT, TimeUnit.SECONDS);
            for (GridServiceManager gsm : gsa.getGridServiceManagers()) {
                String hostAddress = gsm.getMachine().getHostAddress();
                gridInfo.getGsmAddresses().add(hostAddress);
            }
            gsa.getLookupServices().waitFor(1, TIMEOUT, TimeUnit.SECONDS);
            for (LookupService ls : gsa.getLookupServices()) {
                String hostAddress = ls.getMachine().getHostAddress();
                gridInfo.getLusAddresses().add(hostAddress);
            }
            gsa.getGridServiceContainers().waitFor(1, TIMEOUT, TimeUnit.SECONDS);
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

    private Map<String, ClusterReplicationPolicy> createClusterReplicationPolicyMap(Spaces spaces) {
        Map<String, ClusterReplicationPolicy> replPolicyMap = new HashMap<>();
        for (Space space : spaces) {
            try {
                IJSpace spaceProxy = space.getGigaSpace().getSpace();
                IRemoteJSpaceAdmin spaceAdmin = (IRemoteJSpaceAdmin) (spaceProxy.getAdmin());
                ReplicationPolicy replicationPolicy = spaceAdmin.getClusterPolicy().getReplicationPolicy();

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
                System.err.printf("Failed to get '%s' space proxy admin", space.getName(), TIMEOUT);
                System.exit(1);
            }
        }
        return replPolicyMap;
    }

    public void printGridStatus(int hostCount, String lookupLocators) {
        Admin admin = null;
        try {
            admin = new AdminFactory().addLocators(lookupLocators).credentials(DEFAULT_USER_NAME, DEFAULT_USER_PASSWORD).createAdmin();
            if (!admin.getGridServiceAgents().waitFor(hostCount, TIMEOUT, TimeUnit.SECONDS)) {
                System.err.printf("%d GSA has been found using lookup_locators: %s during %d seconds", hostCount, lookupLocators, TIMEOUT);
                System.exit(1);
            }

            GridInfo gridInfo = createGridInfo(admin);
            System.out.println(converter.convert(gridInfo));

            Map<String, ClusterReplicationPolicy> replPolicyMap = createClusterReplicationPolicyMap(admin.getSpaces());
            for (Entry<String, ClusterReplicationPolicy> entry : replPolicyMap.entrySet()) {
                System.out.println(converter.convert(entry.getKey(), entry.getValue()));
            }
        } finally {
            if (admin != null) {
                admin.close();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < PARAM_COUNT) {
            System.err.println("Invalid param count. Usage: grid-inspector.jar [count] [lookup_locators]");
            System.exit(2);
        }
        GridInspector gridInspector = new GridInspector();
        int count = 0;
        try {
            count = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.printf("Invalid param %s. Usage: grid-inspector.jar [count] [lookup_locators]", args[0]);
            System.exit(2);
        }
        gridInspector.printGridStatus(count, args[1]);
    }
}
