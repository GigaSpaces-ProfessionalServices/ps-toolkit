package com.gigaspaces.gigapro.rebalancing.gsa.rebalancer;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class StatefulProcessingUnitRebalancer extends ProcessingUnitRebalancer{

    private static Logger logger = LoggerFactory.getLogger(StatefulProcessingUnitRebalancer.class);

    public StatefulProcessingUnitRebalancer(Admin admin, String puName) {
        super(admin, puName);
    }

    protected void doRebalancing(ProcessingUnit processingUnit, List<GridServiceAgent> gsas) {
        for (int i = 0; i < MAX_REBALANCING_COUNT; i++){
            //get empty containers
            Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap = buildEmptyContainersMap(gsas);

            //how many unbalanced nodes can be?
            int initialPartitionCount = processingUnit.getPartitions().length;

            int instancesPerAgent = initialPartitionCount / gsas.size();
            int unbalancedNodes = initialPartitionCount % gsas.size();

            //check primaries
            Map<GridServiceAgent, Integer> lowPrimaries = new HashMap<>();
            Map<GridServiceAgent, Integer> highPrimaries = new HashMap<>();
            boolean unbalanced = false;
            for (GridServiceAgent gsa : gsas){
                int primaries = listPrimariesOnGSA(gsa).size();
                if (primaries > instancesPerAgent) {
                    highPrimaries.put(gsa, primaries);
                }
                if (primaries > instancesPerAgent + 1){
                    unbalanced = true;
                }
                if (primaries < instancesPerAgent){
                    lowPrimaries.put(gsa, primaries);
                }
            }

            if (!unbalanced && highPrimaries.size() == unbalancedNodes){

                // balance backups
                logger.info(puName + " is balanced on primaries. Starting backup rebalancing...");
                doRebalancingOfBackups(processingUnit, gsas);
                return;
            }

            boolean moved = quickSwapRestart(lowPrimaries, highPrimaries);

            if (!moved){
                moved = moveBackupToLowPrimaryGSARestart(emptyContainersMap, instancesPerAgent, highPrimaries, lowPrimaries);
            }

            if (!moved){
                logger.error("rebalance failed for PU " + puName);
            }
        }
    }

    private void doRebalancingOfBackups(ProcessingUnit processingUnit, List<GridServiceAgent> gsas) {
        for (int i = 0; i < MAX_REBALANCING_COUNT; i++){
            //get empty containers
            Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap = buildEmptyContainersMap(gsas);

            //how many unbalanced nodes can be?
            int initialPartitionCount = processingUnit.getPartitions().length;
            int instancesPerAgent = initialPartitionCount / gsas.size();
            int unbalancedNodes = initialPartitionCount % gsas.size();

            //check primaries
            Map<GridServiceAgent, Integer> lowBackups = new HashMap<>();
            Map<GridServiceAgent, Integer> highBackups = new HashMap<>();
            boolean unbalanced = false;

            for (GridServiceAgent gsa : gsas){
                int backups = listBackupsOnGSA(gsa).size();

                if (backups > instancesPerAgent) {
                    highBackups.put(gsa, backups);
                }
                if ((listPrimariesOnGSA(gsa).size() > instancesPerAgent) || (backups > instancesPerAgent + unbalancedNodes)){
                    // node contains more primaries
                    unbalanced = true;
                }
                if (backups < getBackupPerAgentCount(instancesPerAgent)){
                    lowBackups.put(gsa, backups);
                }
            }

            if (!unbalanced){
                logger.info(puName + " is balanced");
                return;
            }

            boolean moved = moveBackupToLowBackupGSA(emptyContainersMap, instancesPerAgent, highBackups, lowBackups);

            if (!moved){
                logger.error("rebalance failed for PU " + puName);
            }
        }
    }



    private boolean moveBackupToLowPrimaryGSARestart(Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap, int instancesPerAgent, Map<GridServiceAgent, Integer> highPrimaries, Map<GridServiceAgent, Integer> lowPrimaries) {
        boolean moved = false;
        for (GridServiceAgent gsa : highPrimaries.keySet()){
            List<ProcessingUnitInstance> processingUnitInstances = listPrimariesOnGSA(gsa);
            for (ProcessingUnitInstance pui : processingUnitInstances){
                Map<GridServiceAgent, GridServiceContainer> availableContainersOnLowPrimaries = findEmptyContainersOnLowPrimaries(emptyContainersMap, pui, instancesPerAgent);
                if (availableContainersOnLowPrimaries.isEmpty()){
                    logger.info("No empty containers found. Looking for available containers");
                    // check for available containers with pu
                    availableContainersOnLowPrimaries = findAvailableContainersOnLowPrimaries(lowPrimaries, pui, instancesPerAgent);

                    if (availableContainersOnLowPrimaries.isEmpty()) {
                        logger.info("No available containers found!!!");
                        continue;
                    }
                }
                moved = moveBackupToAvailableContainer(pui, availableContainersOnLowPrimaries);
                break;
            }
            if (moved) break;
        }
        return moved;
    }

    private boolean moveBackupToLowBackupGSA(Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap, int instancesPerAgent, Map<GridServiceAgent, Integer> highBackups, Map<GridServiceAgent, Integer> lowBackups) {
        boolean moved = false;
        for (GridServiceAgent gsa : highBackups.keySet()){
            List<ProcessingUnitInstance> processingUnitInstances = listBackupsOnGSA(gsa);
            for (ProcessingUnitInstance pui : processingUnitInstances){
                Map<GridServiceAgent, GridServiceContainer> availableContainersOnLowBackups = findEmptyContainersOnLowBackup(emptyContainersMap, pui, instancesPerAgent);
                if (availableContainersOnLowBackups.isEmpty()){
                    logger.info("No empty containers found. Looking for available containers");
                    // check for available containers with pu
                    availableContainersOnLowBackups = findAvailableContainersOnLowBackups(lowBackups, pui, instancesPerAgent);

                    if (availableContainersOnLowBackups.isEmpty()) {
                        logger.info("No available containers found!!!");
                        continue;
                    }
                }
                moved = moveBackupToAvailableContainer(pui, availableContainersOnLowBackups, false);
                break;
            }
            if (moved) break;
        }
        return moved;
    }

    private boolean moveBackupToAvailableContainer(ProcessingUnitInstance pui, Map<GridServiceAgent, GridServiceContainer> availableContainers) {
        return moveBackupToAvailableContainer(pui, availableContainers, true);
    }

    private boolean moveBackupToAvailableContainer(ProcessingUnitInstance pui, Map<GridServiceAgent, GridServiceContainer> availableContainers, boolean restart) {
        GridServiceAgent targetGSA = availableContainers.keySet().iterator().next();
        String currentBackupGSA = pui.getSpaceInstance().getPartition().getBackup().getMachine().getHostName();
        logger.info(String.format("moving backup id=%d from %s to %s",
                pui.getInstanceId(), currentBackupGSA, targetGSA.getMachine().getHostName()));
        ProcessingUnitInstance relocatedInstance = findBackupInstanceForPrimary(pui).relocateAndWait(availableContainers.get(targetGSA));

        waitForInstanceInit(relocatedInstance);

        if (restart) {
            pui.restartAndWait();
        }
        return true;
    }


    private Map<GridServiceAgent, GridServiceContainer> findAvailableContainersOnLowPrimaries(Map<GridServiceAgent, Integer> lowPrimaries, ProcessingUnitInstance primary, int instancesPerAgent) {
        Map<GridServiceAgent, GridServiceContainer> result = new HashMap<>();

        for (Map.Entry<GridServiceAgent, Integer> gridServiceAgentToPrimaries : lowPrimaries.entrySet()) {
            for (GridServiceContainer gsc : gridServiceAgentToPrimaries.getKey().getMachine().getGridServiceContainers()) {
                if (isContainerEligibleForPrimary(gsc, primary, instancesPerAgent)){
                    result.put(gridServiceAgentToPrimaries.getKey(), gsc);
                    logger.info(String.format("added %s at %s machine as primary target", gsc.getUid(), gsc.getMachine().getHostName()));
                }
            }
        }
        return result;
    }

    private Map<GridServiceAgent, GridServiceContainer> findAvailableContainersOnLowBackups(Map<GridServiceAgent, Integer> lowBackups, ProcessingUnitInstance backup, int instancesPerAgent) {
        Map<GridServiceAgent, GridServiceContainer> result = new HashMap<>();

        for (Map.Entry<GridServiceAgent, Integer> gridServiceAgentToPrimaries : lowBackups.entrySet()) {
            for (GridServiceContainer gsc : gridServiceAgentToPrimaries.getKey().getMachine().getGridServiceContainers()) {
                if (isContainerEligibleForBackup(gsc, backup, instancesPerAgent)){
                    result.put(gridServiceAgentToPrimaries.getKey(), gsc);
                    logger.info(String.format("added %s at %s machine as backup target", gsc.getUid(), gsc.getMachine().getHostName()));
                }
            }
        }
        return result;
    }

    private boolean quickSwapRestart(Map<GridServiceAgent, Integer> lowPrimaries, Map<GridServiceAgent, Integer> highPrimaries) {
        boolean moved = false;
        for (GridServiceAgent gsa : highPrimaries.keySet()){
            List<ProcessingUnitInstance> processingUnitInstances = listPrimariesOnGSA(gsa);
            for (ProcessingUnitInstance pui : processingUnitInstances){
//                Look for a quick swap -- if backup on low primary machine, restart primary
                for (GridServiceAgent lowPrimaryGSA : lowPrimaries.keySet()){
                    if (backupIsOnLowPrimaryGSA(pui, lowPrimaryGSA)){
                        logger.info(String.format("restarting %s primary at machine %s", pui.getName(), pui.getMachine().getHostName()));
                        pui.restartAndWait();
                        moved = true;
                        break;
                    }   else {
                        logger.info(String.format("backup for %s is not found on low-primary GSA", pui.getName()));
                    }
                }
                if (moved) break;
            }
            if (moved) break;
        }
        return moved;
    }

    private boolean backupIsOnLowPrimaryGSA(ProcessingUnitInstance pui, GridServiceAgent lowPrimaryGSA) {
        return lowPrimaryGSA.getMachine().getHostName().equals(pui.getSpaceInstance().getPartition().getBackup().getMachine().getHostName());
    }

    private List<ProcessingUnitInstance> listPrimariesOnGSA(GridServiceAgent gsa) {
        List<ProcessingUnitInstance> primaries = new ArrayList<>();
        GridServiceContainers gscs = gsa.getMachine().getGridServiceContainers();
        gscs.waitFor(1, 2, TimeUnit.SECONDS);

        Arrays.stream(gscs.getContainers()).forEach(gsc -> primaries.addAll(listPrimariesOnGSC(gsc, puName)));

        return primaries;
    }

    private List<ProcessingUnitInstance> listBackupsOnGSA(GridServiceAgent gsa) {
        List<ProcessingUnitInstance> backups = new ArrayList<>();
        GridServiceContainers gscs = gsa.getMachine().getGridServiceContainers();
        gscs.waitFor(1, 2, TimeUnit.SECONDS);

        Arrays.stream(gscs.getContainers()).forEach(gsc -> backups.addAll(listBackupsOnGSC(gsc, puName)));

        return backups;
    }

    private Map<GridServiceAgent, GridServiceContainer> findEmptyContainersOnLowPrimaries(
            Map<GridServiceAgent, List<GridServiceContainer>> emptyContainers, ProcessingUnitInstance primary, int instancesPerAgent){
        Map<GridServiceAgent, GridServiceContainer> result = new HashMap<>();
        for (Map.Entry<GridServiceAgent, List<GridServiceContainer>> gsaToGsc : emptyContainers.entrySet()){
            GridServiceContainer container = gsaToGsc.getValue().get(0);
            if (isContainerEligibleForPrimary(container, primary, instancesPerAgent)){
                result.put(gsaToGsc.getKey(), container);
                logger.info(String.format("added %s at %s machine as primary target", container.getUid(), container.getMachine().getHostName()));
            }
        }
        return result;
    }

    private Map<GridServiceAgent, GridServiceContainer> findEmptyContainersOnLowBackup(
            Map<GridServiceAgent, List<GridServiceContainer>> emptyContainers, ProcessingUnitInstance backup, int instancesPerAgent){
        Map<GridServiceAgent, GridServiceContainer> result = new HashMap<>();
        for (Map.Entry<GridServiceAgent, List<GridServiceContainer>> gsaToGsc : emptyContainers.entrySet()){
            GridServiceContainer container = gsaToGsc.getValue().get(0);
            if (isContainerEligibleForBackup(container, backup, instancesPerAgent)){
                result.put(gsaToGsc.getKey(), container);
                logger.info(String.format("added %s at %s machine as backup target", container.getUid(), container.getMachine().getHostName()));
            }
        }
        return result;
    }

    private ProcessingUnitInstance findBackupInstanceForPrimary(ProcessingUnitInstance primary){
        return primary.getPartition().getBackup();
    }

    private boolean isContainerEligibleForPrimary(GridServiceContainer gsc, ProcessingUnitInstance primary, int instancesPerAgent){
        return lowPrimaryAgent(gsc, instancesPerAgent) && notTheSameAgentAsBackup(gsc, primary);
    }

    private boolean isContainerEligibleForBackup(GridServiceContainer gsc, ProcessingUnitInstance backup, int instancesPerAgent){
        return lowBackupAgent(gsc, instancesPerAgent) && notTheSameAgentAsPrimary(gsc, backup);
    }

    private boolean notTheSameAgentAsBackup(GridServiceContainer gsc, ProcessingUnitInstance primary) {
        return !primary.getSpaceInstance().getPartition().getBackup().getMachine().getHostAddress().equals(gsc.getMachine().getHostAddress());
    }

    private boolean notTheSameAgentAsPrimary(GridServiceContainer gsc, ProcessingUnitInstance backup) {
        return !backup.getSpaceInstance().getPartition().getPrimary().getMachine().getHostAddress().equals(gsc.getMachine().getHostAddress());
    }

    private boolean lowPrimaryAgent(GridServiceContainer gsc, int instancesPerAgent) {
        return listPrimariesOnGSA(gsc.getGridServiceAgent()).size() < instancesPerAgent;
    }

    private boolean lowBackupAgent(GridServiceContainer gsc, int instancesPerAgent) {
        return listBackupsOnGSA(gsc.getGridServiceAgent()).size() < getBackupPerAgentCount(instancesPerAgent);
    }

}
