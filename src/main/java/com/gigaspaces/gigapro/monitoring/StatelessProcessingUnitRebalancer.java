package com.gigaspaces.gigapro.monitoring;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class StatelessProcessingUnitRebalancer extends ProcessingUnitRebalancer{

    public StatelessProcessingUnitRebalancer(Admin admin, String puName) {
        super(admin, puName);
    }

    @Override
    protected void doRebalancing(ProcessingUnit processingUnit, List<GridServiceAgent> gsas) {
        for (int i = 0; i < MAX_REBALANCING_COUNT; i++){
            //get empty containers
            Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap = buildEmptyContainersMap(gsas);

            //how many unbalanced nodes can be?
            int instancesPerAgent = processingUnit.getInstances().length / gsas.size();
            int unbalancedNodes = processingUnit.getInstances().length % gsas.size();

            //check primaries
            Map<GridServiceAgent, Integer> lowInstances = new HashMap<>();
            Map<GridServiceAgent, Integer> highinstances = new HashMap<>();
            boolean unbalanced = false;
            for (GridServiceAgent gsa : gsas){
                int instances = listInstancesOnGSA(gsa).size();
                if (instances > instancesPerAgent){
                    highinstances.put(gsa, instances);
                }
                if (instances > instancesPerAgent + 1){
                    unbalanced = true;
                }
                if (instances < instancesPerAgent){
                    lowInstances.put(gsa, instances);
                }
            }

            if (!unbalanced && highinstances.size() == unbalancedNodes){
                System.out.println(puName + " is balanced");
                return;
            }

            boolean moved = quickSwapRestart(lowInstances, highinstances, emptyContainersMap);

            if (!moved){
                System.out.println("rebalance failed for PU " + puName);
            }
        }
    }

    private boolean quickSwapRestart(Map<GridServiceAgent, Integer> lowInstances, Map<GridServiceAgent, Integer> highInstances, Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap) {
        boolean moved = false;
        for (GridServiceAgent gsa : highInstances.keySet()){
            List<ProcessingUnitInstance> processingUnitInstances = listInstancesOnGSA(gsa);
            for (ProcessingUnitInstance pui : processingUnitInstances){
                 for (GridServiceAgent lowPrimaryGSA : lowInstances.keySet()){
                    GridServiceContainer targetGSC = getEmptyContainer(emptyContainersMap, lowPrimaryGSA);
                    if (targetGSC != null){
                        pui.relocateAndWait(targetGSC);
                        moved = true;
                        break;
                    }
                }
                if (moved) break;
            }
            if (moved) break;
        }
        return moved;
    }

    private GridServiceContainer getEmptyContainer(Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap, GridServiceAgent lowPrimaryGSA) {
        GridServiceContainer gsc = null;
        List<GridServiceContainer> emptyGscs = emptyContainersMap.get(lowPrimaryGSA);
        if (emptyGscs != null && emptyGscs.size() != 0){
            gsc = emptyGscs.get(0);
        }
        return gsc;
    }

    private List<ProcessingUnitInstance> listInstancesOnGSA(GridServiceAgent gsa) {
        List<ProcessingUnitInstance> primaries = new ArrayList<>();
        GridServiceContainers gscs = gsa.getMachine().getGridServiceContainers();
        gscs.waitFor(1, 2, TimeUnit.SECONDS);
        for (GridServiceContainer gsc : gscs.getContainers()){
            primaries.addAll(Arrays.asList(gsc.getProcessingUnitInstances(puName)));
        }
        return primaries;

    }

}
