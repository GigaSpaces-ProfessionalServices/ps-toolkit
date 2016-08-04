package com.gigaspaces.gigapro;

import com.gigaspaces.cluster.replication.MirrorServiceConfig;
import com.gigaspaces.cluster.replication.sync.SyncReplPolicy;
import com.gigaspaces.gigapro.convert.Converter;
import com.gigaspaces.gigapro.convert.PropertiesFormatConverter;
import com.gigaspaces.gigapro.model.ClusterReplicationPolicy;
import com.gigaspaces.gigapro.model.GridInfo;
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
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.Spaces;
import org.openspaces.admin.vm.VirtualMachine;

import java.io.Console;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class GridInfoService {

    private static final int DEFAULT_GSA_COUNT = 1;
    private static final long DEFAULT_TIMEOUT = 10l;
    private static final String COLON_SEPATATOR = ":";

    private static final String GSA_COUNT_KEY = "gsa.count";
    private static final String LOOKUP_LOCATORS_KEY = "lookup.locators";
    private static final String LOOKUP_GROUPS_KEY = "lookup.groups";
    private static final String XAP_USER_NAME_KEY = "xap.user.name";
    private static final String WAIT_TIMEOUT_KEY = "wait.timeout";

    private final Converter converter = new PropertiesFormatConverter();

    private GridInfo createGridInfo(Admin admin, long waitTimeout) {
        GridInfo gridInfo = new GridInfo();

        gridInfo.setLookupGroups(new HashSet<String>(Arrays.<String> asList(admin.getGroups())));
        for (LookupLocator ll : admin.getLocators()) {
            gridInfo.getLookupLocators().add(ll.getHost() + COLON_SEPATATOR + ll.getPort());
        }

        for (VirtualMachine virtualMachine : admin.getVirtualMachines()) {
            gridInfo.getIpAddresses().add(virtualMachine.getMachine().getHostAddress());
        }

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

    private Map<String, ClusterReplicationPolicy> createClusterReplicationPolicyMap(Spaces spaces) {
        Map<String, ClusterReplicationPolicy> replPolicyMap = new HashMap<>();
        for (Space space : spaces) {
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
                System.err.printf("Failed to get '%s' space proxy admin", space.getName(), DEFAULT_TIMEOUT);
                System.exit(1);
            }
        }
        return replPolicyMap;
    }

    public GridInfo getGridInfo(int count, String username, char[] password, String lookupGroups, String lookupLocators, long waitTimeout) {
        Admin admin = null;
        try {
            AdminFactory adminFactory = new AdminFactory();
            if (lookupLocators != null && !lookupLocators.isEmpty()) {
                adminFactory.addLocators(lookupLocators);
            }
            if (lookupGroups != null && !lookupGroups.isEmpty()) {
                adminFactory.addGroups(lookupGroups);
            }
            if (username != null && !username.isEmpty() && password != null && password.length > 0) {
                adminFactory.credentials(username, String.valueOf(password));
                Arrays.fill(password, ' ');
            }
            admin = adminFactory.createAdmin();

            if (!admin.getGridServiceAgents().waitFor(count, waitTimeout, TimeUnit.SECONDS)) {
                System.err.printf("%d GSA have not been found using during %d seconds", count, waitTimeout);
                return null;
            }

            GridInfo gridInfo = createGridInfo(admin, waitTimeout);
            gridInfo.setReplPolicyMap(createClusterReplicationPolicyMap(admin.getSpaces()));

            return gridInfo;
        } finally {
            if (admin != null) {
                admin.close();
            }
        }
    }

    public void printGridStatus(GridInfo gridInfo) {
        System.out.println(converter.convert(gridInfo));

        for (Entry<String, ClusterReplicationPolicy> entry : gridInfo.getReplPolicyMap().entrySet()) {
            System.out.println(converter.convert(entry.getKey(), entry.getValue()));
        }
    }

    public static void main(String[] args) {
        String lookupLocators = System.getProperty(LOOKUP_LOCATORS_KEY);
        String lookupGroups = System.getProperty(LOOKUP_GROUPS_KEY);

        String timeoutStr = System.getProperty(WAIT_TIMEOUT_KEY);
        long timeout = DEFAULT_TIMEOUT;
        if (timeoutStr != null) {
            try {
                timeout = Long.parseLong(timeoutStr);
            } catch (NumberFormatException e) {
                System.err.printf("Invalid wait timeout provided: %s. The default timeout %d will be used.", timeoutStr, DEFAULT_TIMEOUT);
            }
        }

        String username = System.getProperty(XAP_USER_NAME_KEY);
        char[] password = null;
        if (username != null) {
            Console console = System.console();
            if (console == null) {
                System.err.printf("No console is associated with the current JVM. Exiting...");
                System.exit(1);
            }
            password = console.readPassword("Password for %s:", username);
        }
        String countStr = System.getProperty(GSA_COUNT_KEY);
        int count = DEFAULT_GSA_COUNT;
        if (countStr != null) {
            try {
                count = Integer.parseInt(countStr);
            } catch (NumberFormatException e) {
                System.err.printf("Invalid gsa count provided: %s", countStr);
            }
        }

        GridInfoService gridInfoService = new GridInfoService();
        GridInfo gridInfo = gridInfoService.getGridInfo(count, username, password, lookupGroups, lookupLocators, timeout);
        if (gridInfo != null) {
            gridInfoService.printGridStatus(gridInfo);
        }
    }
}
