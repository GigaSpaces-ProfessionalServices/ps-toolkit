package com.gigaspaces.gigapro.rebalancing.gsc.rebalancer;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Denys_Novikov
 * Date: 05.04.2018
 */
public class StatefulAgentProcessingUnitRebalancer extends AgentProcessingUnitRebalancer {

    private static Logger logger = LoggerFactory.getLogger(StatefulAgentProcessingUnitRebalancer.class);

    public StatefulAgentProcessingUnitRebalancer(Admin admin, String puName) {
        super(admin, puName);
    }

    @Override
    protected void doRebalancingBetweenContainers(GridServiceAgent gsa) {
        for (int i = 0; i < MAX_REBALANCING_COUNT; i++){
            //get empty containers for single gsa
            Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap = buildEmptyContainersMap(Collections.singletonList(gsa));

            // count instance for particular processing unit
            int totalPrimaryInstancesPerAgent = getInstancesCount(gsa, SpaceMode.PRIMARY);

            if (totalPrimaryInstancesPerAgent == 0)
                // no instances deployed
                return;

            int containersCount = gsa.getMachine().getGridServiceContainers().getContainers().length;

            int instancesPerContainer = totalPrimaryInstancesPerAgent / containersCount;
            int unbalancedNodes = totalPrimaryInstancesPerAgent % containersCount;

            //check primaries
            Map<GridServiceContainer, Integer> lowPrimaries = new HashMap<>();
            Map<GridServiceContainer, Integer> highPrimaries = new HashMap<>();
            boolean unbalanced = false;
            for (GridServiceContainer gsc : gsa.getMachine().getGridServiceContainers().getContainers()){
                int primaries = listPrimariesOnGSC(gsc, puName).size();

                if (primaries > instancesPerContainer) {
                    highPrimaries.put(gsc, primaries);
                }
                if (primaries > instancesPerContainer + 1){
                    unbalanced = true;
                }
                if (primaries < instancesPerContainer){
                    lowPrimaries.put(gsc, primaries);
                }
            }

            if (!unbalanced && highPrimaries.size() == unbalancedNodes){

                // balance backups
                logger.info(puName + " is balanced on primaries. Starting backup rebalancing...");
                doRebalancingOfBackupsBetweenContainers(gsa, instancesPerContainer);
                return;
            }

            boolean moved = movePrimaryToLowPrimaryGSC(emptyContainersMap, instancesPerContainer, highPrimaries, lowPrimaries);

            if (!moved){
                logger.error("rebalance failed for PU " + puName);
            }

        }
    }

    private void doRebalancingOfBackupsBetweenContainers(GridServiceAgent gsa, int primaryInstancesPerContainer) {
        for (int i = 0; i < MAX_REBALANCING_COUNT; i++){
            //get empty containers
            Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap = buildEmptyContainersMap(Collections.singletonList(gsa));

            int totalBackupInstancesPerAgent = getInstancesCount(gsa, SpaceMode.BACKUP);

            if (totalBackupInstancesPerAgent == 0)
                // no instances deployed
                return;

            int containersCount = gsa.getMachine().getGridServiceContainers().getContainers().length;

            int instancesPerContainer = totalBackupInstancesPerAgent / containersCount;
            int unbalancedNodes = totalBackupInstancesPerAgent % containersCount;


            //check primaries
            Map<GridServiceContainer, Integer> lowBackups = new HashMap<>();
            Map<GridServiceContainer, Integer> highBackups = new HashMap<>();
            boolean unbalanced = false;

            for (GridServiceContainer gsc : gsa.getMachine().getGridServiceContainers().getContainers()){
                int backups = listBackupsOnGSC(gsc, puName).size();

                if (backups > instancesPerContainer) {
                    highBackups.put(gsc, backups);
                }

                if (gscContainsUnbalancedPrimaryAndBackupInstances(primaryInstancesPerContainer, instancesPerContainer, unbalancedNodes, gsc)) {
                    // node contains more primaries
                    unbalanced = true;
                }
                if (backups < getBackupPerAgentCount(instancesPerContainer)){
                    lowBackups.put(gsc, backups);
                }
            }

            logger.info("unbalanced " + unbalanced);
            if (!unbalanced){
                logger.info(puName + " is balanced");
                return;
            }

            boolean moved = moveBackupToLowBackupGSC(emptyContainersMap, highBackups, lowBackups);

            if (!moved){
                logger.error("rebalance failed for PU " + puName);
            }
        }
    }

    /**
     * method checks if unbalanced primary instance (if is) is on the same GSC with unbalanced backup instance
     * @param primaryInstancesPerContainer primaries per container count
     * @param backupInstancesPerContainer backups per container count
     * @param unbalancedNodes unbalanced backup instances count
     * @param gsc GSC to be checked
     * @return true if provided GSC is overloaded
     */
    private boolean gscContainsUnbalancedPrimaryAndBackupInstances(int primaryInstancesPerContainer, int backupInstancesPerContainer, int unbalancedNodes, GridServiceContainer gsc) {
        return (listPrimariesOnGSC(gsc, puName).size() + listBackupsOnGSC(gsc, puName).size()) >
                (primaryInstancesPerContainer + backupInstancesPerContainer + unbalancedNodes);
    }

    private boolean movePrimaryToLowPrimaryGSC(Map<GridServiceAgent, List<GridServiceContainer>> emptyContainers, int instancesPerContainer, Map<GridServiceContainer, Integer> highPrimaries, Map<GridServiceContainer, Integer> lowPrimaries) {
        boolean moved = false;
        for (GridServiceContainer gsc : highPrimaries.keySet()){
            List<ProcessingUnitInstance> processingUnitInstances = listPrimariesOnGSC(gsc, puName);
            for (ProcessingUnitInstance pui : processingUnitInstances){
                Iterator<List<GridServiceContainer>> iterator = emptyContainers.values().iterator();
                GridServiceContainer availableContainerOnLowPrimaries = findEmptyAddedContainerOnLowPrimaries(iterator.hasNext() ? iterator.next() : Collections.emptyList());
                if (availableContainerOnLowPrimaries == null){
                    logger.info("No empty containers found. Looking for available containers");
                    // check for available containers with pu
                    availableContainerOnLowPrimaries = findAvailableContainerOnLowPrimaries(lowPrimaries, instancesPerContainer);

                    if (availableContainerOnLowPrimaries == null) {
                        logger.info("No available containers found!!!");
                        continue;
                    }
                }

                moved = movePrimaryToAvailableContainerWithinGSA(pui, availableContainerOnLowPrimaries);
                break;
            }
            if (moved) break;
        }
        return moved;
    }

    private boolean moveBackupToLowBackupGSC(Map<GridServiceAgent, List<GridServiceContainer>> emptyContainers, Map<GridServiceContainer, Integer> highBackups, Map<GridServiceContainer, Integer> lowBackups) {
        boolean moved = false;
        for (GridServiceContainer gsc : highBackups.keySet()){
            List<ProcessingUnitInstance> processingUnitInstances = listBackupsOnGSC(gsc, puName);
            for (ProcessingUnitInstance pui : processingUnitInstances){
                Iterator<List<GridServiceContainer>> iterator = emptyContainers.values().iterator();
                GridServiceContainer availableContainerOnLowBackups = findEmptyAddedContainerOnLowBackups(iterator.hasNext() ? iterator.next() : Collections.emptyList());
                if (availableContainerOnLowBackups == null){
                    logger.info("No empty containers found. Looking for available containers");
                    // check for available containers with pu
                    availableContainerOnLowBackups = findAvailableContainerOnLowBackups(lowBackups);

                    if (availableContainerOnLowBackups == null) {
                        logger.info("No available containers found!!!");
                        continue;
                    }
                }

                moved = moveBackupToAvailableContainerWithinGSA(pui, availableContainerOnLowBackups);
                break;
            }
            if (moved) break;
        }
        return moved;
    }

    private boolean movePrimaryToAvailableContainerWithinGSA(ProcessingUnitInstance pui, GridServiceContainer availableContainer) {
        logger.info(String.format("moving primary id=%d from %s to %s",
                pui.getInstanceId(), pui.getGridServiceContainer().getUid(), availableContainer.getUid()));
        ProcessingUnitInstance relocatedInstance = pui.relocateAndWait(availableContainer);
        waitForInstanceInit(relocatedInstance);

        relocatedInstance.getPartition().getPrimary().restartAndWait();
        waitForInstanceInit(relocatedInstance);

        return true;
    }

    private boolean moveBackupToAvailableContainerWithinGSA(ProcessingUnitInstance pui, GridServiceContainer availableContainer) {
        logger.info(String.format("moving backup id=%d from %s to %s",
                pui.getInstanceId(), pui.getGridServiceContainer().getUid(), availableContainer.getUid()));
        ProcessingUnitInstance relocatedInstance = pui.relocateAndWait(availableContainer);
        waitForInstanceInit(relocatedInstance);

        return true;
    }

    private GridServiceContainer findEmptyAddedContainerOnLowPrimaries(List<GridServiceContainer> emptyContainers){
        for (GridServiceContainer container : emptyContainers) {
            if (listPrimariesOnGSC(container, puName).size() == 0) {
                logger.info(String.format("added %s at %s machine as primary target", container.getUid(), container.getMachine().getHostName()));
                return container;
            }
        }
        return null;
    }

    private GridServiceContainer findAvailableContainerOnLowPrimaries(Map<GridServiceContainer, Integer> lowPrimaries, int instancesPerContainer) {
        for (Map.Entry<GridServiceContainer, Integer> gridServiceAgentToPrimaries : lowPrimaries.entrySet()) {
            GridServiceContainer gsc = gridServiceAgentToPrimaries.getKey();
            if (listPrimariesOnGSC(gsc, puName).size() < instancesPerContainer) {
                logger.info(String.format("added %s at %s machine as primary target", gsc.getUid(), gsc.getMachine().getHostName()));
                return gsc;
            }
        }
        return null;
    }

    private GridServiceContainer findEmptyAddedContainerOnLowBackups(List<GridServiceContainer> emptyContainers){
        for (GridServiceContainer container : emptyContainers) {
            if (listBackupsOnGSC(container, puName).size() == 0) {
                logger.info(String.format("added %s at %s machine as backup target", container.getUid(), container.getMachine().getHostName()));
                return container;
            }
        }
        return null;
    }

    /**
     * returns the most underloaded GSC
     * @param lowBackups GSCs mapped to it's backup instances count
     * @return the most underloaded GSC
     */
    private GridServiceContainer findAvailableContainerOnLowBackups(Map<GridServiceContainer, Integer> lowBackups) {
        if (lowBackups.isEmpty()) {
            return null;
        }
        OptionalInt minCount = lowBackups.values().stream().mapToInt(count -> count).min();

        if (!minCount.isPresent()) {
            return null;
        }

        for (Map.Entry<GridServiceContainer, Integer> gridServiceAgentToBackups : lowBackups.entrySet()) {
            GridServiceContainer gsc = gridServiceAgentToBackups.getKey();
            if (listBackupsOnGSC(gsc, puName).size() == minCount.getAsInt()) {
                logger.info(String.format("added %s at %s machine as backup target", gsc.getUid(), gsc.getMachine().getHostName()));
                return gsc;
            }
        }
        return null;
    }

    private int getInstancesCount(GridServiceAgent gsa, SpaceMode spaceMode) {
        return Arrays.stream(gsa.getMachine().getGridServiceContainers().getContainers())
                .mapToInt(gsc -> Arrays.stream(gsc.getProcessingUnitInstances(puName))
                        .filter(processingUnitInstance -> processingUnitInstance.getSpaceInstance().getMode() == spaceMode)
                        .collect(Collectors.toList()).size()).sum();
    }

}
