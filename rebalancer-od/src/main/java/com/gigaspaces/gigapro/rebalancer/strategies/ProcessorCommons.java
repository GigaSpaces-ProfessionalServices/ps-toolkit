package com.gigaspaces.gigapro.rebalancer.strategies;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class ProcessorCommons {

    /**
     * @param agent GSA to find processing instances in
     * @return list of all processing instances in agent
     */
    public static List<ProcessingUnitInstance> findInstances(GridServiceAgent agent, String processingUnitName) {
        return stream(agent.getGridServiceContainers().getContainers()).flatMap(c -> stream(c.getProcessingUnitInstances(processingUnitName))).collect(toList());
    }

    /**
     * @param agent GSA to find empty GSCs in
     * @return list of empty GSCs in agent
     */
    public static List<GridServiceContainer> findEmptyContainers(GridServiceAgent agent) {
        return stream(agent.getGridServiceContainers().getContainers()).filter(c -> c.getProcessingUnitInstances().length == 0).collect(toList());
    }

    /**
     * @param gridServiceAgents GSAs to find GSCs in
     * @return list of all GSCs in gridServiceAgents
     */
    public static List<GridServiceContainer> findAllContainers(List<GridServiceAgent> gridServiceAgents) {
        return gridServiceAgents.stream().flatMap(a -> stream(a.getGridServiceContainers().getContainers())).collect(toList());
    }
}
