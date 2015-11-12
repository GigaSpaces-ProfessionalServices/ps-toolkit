package com.gigaspaces.gigapro.monitoring;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.apache.commons.collections.MapUtils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class StatefulProcessingUnitRebalancer {

    private int MAX_REBALANCING_COUNT = 10;

    private Admin admin;

    private String puName;

    public StatefulProcessingUnitRebalancer(Admin admin, String puName) {
        this.admin = admin;
        this.puName = puName;
    }

    public void rebalance(){
        //get PU
        ProcessingUnit processingUnit = getProcessingUnit(puName);

        // get GSAa with zone
        List<GridServiceAgent> gsas = getGridServiceAgents(processingUnit.getRequiredContainerZones().getZones());

        for (int i = 0; i < MAX_REBALANCING_COUNT; i++){
            //get empty containers
            Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap = buildEmptyContainersMap(gsas);

            // check PU is deployed
            checkProcessingUnitDeployment(processingUnit);

            //how many unbalanced nodes can be?
            int instancesPerAgent = processingUnit.getPartitions().length / gsas.size();
            int unbalancedNodes = processingUnit.getPartitions().length % gsas.size();

            //check primaries
            Map<GridServiceAgent, Integer> lowPrimaries = new HashMap<>();
            Map<GridServiceAgent, Integer> highPrimaries = new HashMap<>();
            boolean unbalanced = false;
            for (GridServiceAgent gsa : gsas){
                int primaries = listPrimariesOnGSA(gsa).size();
                if (primaries > instancesPerAgent){
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
                System.out.println(puName + " is balanced");
                return;
            }

            boolean moved = quickSwapRestart(lowPrimaries, highPrimaries);

            if (!moved){
                moved = moveBackupToLowPrimaryGSARestart(emptyContainersMap, instancesPerAgent, highPrimaries);
            }

            if (!moved){
                System.out.println("rebalance failed for PU " + puName);
            }
        }
    }

    private boolean moveBackupToLowPrimaryGSARestart(Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap, int instancesPerAgent, Map<GridServiceAgent, Integer> highPrimaries) {
        boolean moved = false;
        for (GridServiceAgent gsa : highPrimaries.keySet()){
            List<ProcessingUnitInstance> processingUnitInstances = listPrimariesOnGSA(gsa);
            for (ProcessingUnitInstance pui : processingUnitInstances){
                Map<GridServiceAgent, GridServiceContainer> emptyContainersOnLowPrimaries = findEmptyContainersOnLowPrimaries(emptyContainersMap, pui, instancesPerAgent);
                if (MapUtils.isEmpty(emptyContainersOnLowPrimaries)){
                    System.out.println();
                    continue;
                }

                GridServiceAgent targetGSA = emptyContainersOnLowPrimaries.keySet().iterator().next();
                String currentBackupGSA = pui.getSpaceInstance().getPartition().getBackup().getMachine().getHostName();
                System.out.println(String.format("moving backup id=%d from %s to %s",
                        pui.getInstanceId(), currentBackupGSA, targetGSA.getMachine().getHostName()));
                findBackupInstanceForPrimary(pui).relocateAndWait(emptyContainersOnLowPrimaries.get(targetGSA));
                System.out.println(String.format("backup on %s restarting primary", targetGSA.getMachine().getHostName()));
                pui.restartAndWait();
                moved = true;
                break;
            }
            if (moved) break;
        }
        return moved;
    }

    private boolean quickSwapRestart(Map<GridServiceAgent, Integer> lowPrimaries, Map<GridServiceAgent, Integer> highPrimaries) {
        boolean moved = false;
        for (GridServiceAgent gsa : highPrimaries.keySet()){
            List<ProcessingUnitInstance> processingUnitInstances = listPrimariesOnGSA(gsa);
            for (ProcessingUnitInstance pui : processingUnitInstances){
//                Look for a quick swap -- if backup on low primary machine, restart primary
                for (GridServiceAgent lowPrimaryGSA : lowPrimaries.keySet()){
                    if (backupIsOnLowPrimaryGSA(pui, lowPrimaryGSA)){
                        System.out.println(String.format("restarting %s primary at machine %s", pui.getName(), pui.getMachine().getHostName()));
                        pui.restartAndWait();
                        moved = true;
                        break;
                    }   else {
                        System.out.println(String.format("backup for %s is not found on low-primary GSA", pui.getName()));
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
        GridServiceContainers gscs = gsa.getGridServiceContainers();
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

    private void checkProcessingUnitDeployment(ProcessingUnit processingUnit) {
        ProcessingUnitInstance[] instances = processingUnit.getInstances();
        if (instances.length < processingUnit.getPlannedNumberOfInstances()){
            //TODO what to do?
            System.out.println("PU is not deployed");
        }
    }

    private Map<GridServiceAgent, List<GridServiceContainer>> buildEmptyContainersMap(List<GridServiceAgent> gsas){
        Map<GridServiceAgent, List<GridServiceContainer>> result = new HashMap<>();
        for (GridServiceAgent gsa : gsas){
            for (GridServiceContainer gsc : gsa.getGridServiceContainers()){
                if (gsc.getProcessingUnitInstances().length == 0){
                    GridServiceAgent gridServiceAgent = gsc.getGridServiceAgent();
                    List<GridServiceContainer> emptyContainers = result.get(gridServiceAgent);
                    if (emptyContainers == null){
                        emptyContainers = new ArrayList<>();
                        result.put(gridServiceAgent, emptyContainers);
                    }
                    emptyContainers.add(gsc);
                }
            }
        }
        return result;
    }

    private Map<GridServiceAgent, GridServiceContainer> findEmptyContainersOnLowPrimaries(
            Map<GridServiceAgent, List<GridServiceContainer>> emptyContainers, ProcessingUnitInstance primary, int instancesPerAgent){
        Map<GridServiceAgent, GridServiceContainer> result = new HashMap<>();
        for (Map.Entry<GridServiceAgent, List<GridServiceContainer>> gsaToGsc : emptyContainers.entrySet()){
            GridServiceContainer container = gsaToGsc.getValue().get(0);
            if (isEmptyContainerEligible(container, primary, instancesPerAgent)){
                result.put(gsaToGsc.getKey(), container);
                System.out.println(String.format("added %s at %s machine as primary target", container.getUid(), container.getMachine().getHostName()));
                System.out.println("primary hostname " + primary.getMachine().getHostName());
                System.out.println("primary backup hostname " + primary.getSpaceInstance().getPartition().getBackup().getMachine().getHostName());
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

    private List<GridServiceAgent> getGridServiceAgents(Set<String> zones) {
        List<GridServiceAgent> result = new ArrayList<>();
        GridServiceAgents gridServiceAgents = admin.getGridServiceAgents();
        gridServiceAgents.waitFor(1);
        for (GridServiceAgent gsa : gridServiceAgents){
            Set<String> gsaZones = gsa.getExactZones().getZones();
            for (String zone : zones){
                if (gsaZones.contains(zone)){
                    result.add(gsa);
                    break;
                }
            }
        }
        return result;
    }

    private ProcessingUnit getProcessingUnit(String puName) {
        return admin.getProcessingUnits().getProcessingUnit(puName);
    }

}
