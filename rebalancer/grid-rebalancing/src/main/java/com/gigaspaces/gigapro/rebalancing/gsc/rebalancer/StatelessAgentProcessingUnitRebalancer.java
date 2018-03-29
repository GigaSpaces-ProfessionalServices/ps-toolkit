package com.gigaspaces.gigapro.rebalancing.gsc.rebalancer;

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
public class StatelessAgentProcessingUnitRebalancer extends AgentProcessingUnitRebalancer {

    private static Logger logger = LoggerFactory.getLogger(StatelessAgentProcessingUnitRebalancer.class);

    protected StatelessAgentProcessingUnitRebalancer(Admin admin, String puName) {
        super(admin, puName);
    }

    @Override
    protected void doRebalancingBetweenContainers(GridServiceAgent gsa) {
        for (int i = 0; i < MAX_REBALANCING_COUNT; i++){
            //get empty containers
            Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap = buildEmptyContainersMap(Collections.singletonList(gsa));

            int totalPrimaryInstancesPerAgent = getInstancesCount(gsa);
            if (totalPrimaryInstancesPerAgent == 0)
                // no instances deployed
                return;

            int containersCount = gsa.getMachine().getGridServiceContainers().getContainers().length;

            int instancesPerContainer = totalPrimaryInstancesPerAgent / containersCount;
            int unbalancedNodes = totalPrimaryInstancesPerAgent % containersCount;

            //check primaries
            Map<GridServiceContainer, Integer> lowInstances = new HashMap<>();
            Map<GridServiceContainer, Integer> highInstances = new HashMap<>();
            boolean unbalanced = false;
            for (GridServiceContainer gsc : gsa.getMachine().getGridServiceContainers().getContainers()){
                int instances = listInstancesOnGSC(gsc).size();
                if (instances > instancesPerContainer){
                    highInstances.put(gsc, instances);
                }
                if (instances > instancesPerContainer + 1){
                    unbalanced = true;
                }
                if (instances < instancesPerContainer){
                    lowInstances.put(gsc, instances);
                }
            }

            if (!unbalanced && highInstances.size() == unbalancedNodes){
                logger.info(puName + " is balanced");
                return;
            }

            boolean moved = quickSwapRestartBetweenContainers(lowInstances, highInstances, emptyContainersMap);

            if (!moved){
                logger.info("rebalance failed for PU " + puName);
            }
        }
    }

    private int getInstancesCount(GridServiceAgent gsa) {
        return Arrays.stream(gsa.getMachine().getGridServiceContainers().getContainers())
                .mapToInt(gsc -> Arrays.stream(gsc.getProcessingUnitInstances(puName))
                        .collect(Collectors.toList()).size()).sum();
    }


    private List<ProcessingUnitInstance> listInstancesOnGSC(GridServiceContainer gsc) {
        return Arrays.asList(gsc.getProcessingUnitInstances(puName));
    }

    private boolean quickSwapRestartBetweenContainers(Map<GridServiceContainer, Integer> lowInstances, Map<GridServiceContainer, Integer> highInstances, Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap) {
        boolean moved = false;
        for (GridServiceContainer highInstanceGSC : highInstances.keySet()){
            List<ProcessingUnitInstance> processingUnitInstances = listInstancesOnGSC(highInstanceGSC);
            for (ProcessingUnitInstance pui : processingUnitInstances){
                for (GridServiceContainer lowInstanceGSC : lowInstances.keySet()){
                    GridServiceContainer targetGSC = getEmptyContainer(emptyContainersMap, lowInstanceGSC);
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


    private GridServiceContainer getEmptyContainer(Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap, GridServiceContainer lowInstanceGSC) {
        GridServiceContainer gsc;
        List<GridServiceContainer> emptyGscs = emptyContainersMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        if (emptyGscs != null && emptyGscs.size() != 0){
            gsc = emptyGscs.get(0);
        } else {
            gsc = lowInstanceGSC;
        }
        return gsc;
    }

}
