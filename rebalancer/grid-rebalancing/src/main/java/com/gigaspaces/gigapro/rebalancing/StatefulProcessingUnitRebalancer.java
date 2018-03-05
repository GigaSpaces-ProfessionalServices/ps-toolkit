package com.gigaspaces.gigapro.rebalancing;

import com.gigaspaces.cluster.activeelection.SpaceMode;
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
            int instancesPerAgent = processingUnit.getPartitions().length / gsas.size();
            int unbalancedNodes = processingUnit.getPartitions().length % gsas.size();

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
                logger.info(puName + " is balanced");
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

    private boolean moveBackupToAvailableContainer(ProcessingUnitInstance pui, Map<GridServiceAgent, GridServiceContainer> availableContainersOnLowPrimaries) {
        GridServiceAgent targetGSA = availableContainersOnLowPrimaries.keySet().iterator().next();
        String currentBackupGSA = pui.getSpaceInstance().getPartition().getBackup().getMachine().getHostName();
        logger.info(String.format("moving backup id=%d from %s to %s",
                pui.getInstanceId(), currentBackupGSA, targetGSA.getMachine().getHostName()));
        logger.error("Primary name: " + pui.getProcessingUnitInstanceName());
        findBackupInstanceForPrimary(pui).relocateAndWait(availableContainersOnLowPrimaries.get(targetGSA));
        logger.info(String.format("backup on %s restarting primary", targetGSA.getMachine().getHostName()));
        pui.restartAndWait();
        return true;
    }

    private Map<GridServiceAgent, GridServiceContainer> findAvailableContainersOnLowPrimaries(Map<GridServiceAgent, Integer> lowPrimaries, ProcessingUnitInstance primary, int instancesPerAgent) {
        Map<GridServiceAgent, GridServiceContainer> result = new HashMap<>();

        for (Map.Entry<GridServiceAgent, Integer> gridServiceAgentToPrimaries : lowPrimaries.entrySet()) {
            for (GridServiceContainer gsc : gridServiceAgentToPrimaries.getKey().getMachine().getGridServiceContainers()) {
                if (isEmptyContainerEligible(gsc, primary, instancesPerAgent)){
                    result.put(gridServiceAgentToPrimaries.getKey(), gsc);
                    logger.info(String.format("added %s at %s machine as primary target", gsc.getUid(), gsc.getMachine().getHostName()));
                    logger.info("primary hostname " + primary.getMachine().getHostName());
                    logger.info("primary backup hostname " + primary.getSpaceInstance().getPartition().getBackup().getMachine().getHostName());
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
        for (GridServiceContainer gsc : gscs.getContainers()){
            for (ProcessingUnitInstance pui : gsc.getProcessingUnitInstances(puName)){
                if (pui.getSpaceInstance().getMode() == SpaceMode.PRIMARY){
                    primaries.add(pui);
                }
            }
        }
        return primaries;
    }

    private Map<GridServiceAgent, GridServiceContainer> findEmptyContainersOnLowPrimaries(
            Map<GridServiceAgent, List<GridServiceContainer>> emptyContainers, ProcessingUnitInstance primary, int instancesPerAgent){
        Map<GridServiceAgent, GridServiceContainer> result = new HashMap<>();
        for (Map.Entry<GridServiceAgent, List<GridServiceContainer>> gsaToGsc : emptyContainers.entrySet()){
            GridServiceContainer container = gsaToGsc.getValue().get(0);
            if (isEmptyContainerEligible(container, primary, instancesPerAgent)){
                result.put(gsaToGsc.getKey(), container);
                logger.info(String.format("added %s at %s machine as primary target", container.getUid(), container.getMachine().getHostName()));
                logger.info("primary hostname " + primary.getMachine().getHostName());
                logger.info("primary backup hostname " + primary.getSpaceInstance().getPartition().getBackup().getMachine().getHostName());
            }
        }
        return result;
    }


    private ProcessingUnitInstance findBackupInstanceForPrimary(ProcessingUnitInstance primary){
        return primary.getPartition().getBackup();
    }

    private boolean isEmptyContainerEligible(GridServiceContainer gsc, ProcessingUnitInstance primary, int instancesPerAgent){
        return lowPrimaryAgent(gsc, instancesPerAgent) &&
                notTheSameAgentAsPrimary(gsc, primary) &&
                notTheSameAgentAsBackup(gsc, primary);
    }

    private boolean notTheSameAgentAsBackup(GridServiceContainer gsc, ProcessingUnitInstance primary) {
        return !primary.getSpaceInstance().getPartition().getBackup().getMachine().getHostAddress().equals(gsc.getMachine().getHostAddress());
    }

    private boolean notTheSameAgentAsPrimary(GridServiceContainer gsc, ProcessingUnitInstance primary) {
        return !gsc.getMachine().getHostAddress().equals(primary.getMachine().getHostAddress());
    }

    private boolean lowPrimaryAgent(GridServiceContainer gsc, int instancesPerAgent) {
        return listPrimariesOnGSA(gsc.getGridServiceAgent()).size() < instancesPerAgent;
    }

}
