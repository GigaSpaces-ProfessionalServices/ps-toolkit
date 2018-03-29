package com.gigaspaces.gigapro.rebalancing;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class DryRunRebalancer {

    private static Logger logger = LoggerFactory.getLogger(DryRunRebalancer.class);
    private final String puName;

    public DryRunRebalancer(String puName) {
        this.puName = puName;
    }

    public boolean doDryRunRebalancing(ProcessingUnit processingUnit, List<GridServiceAgent> gsas) {

        if (processingUnit.getInstances().length == 1) {
            // may be considered as balanced
            logger.info(puName + " has 1 instance");
            return true;
        }


        //how many unbalanced nodes can be?
        int initialPartitionCount = processingUnit.getPartitions().length;

        int instancesPerAgent = initialPartitionCount / gsas.size();
        int unbalancedNodes = initialPartitionCount % gsas.size();

        //check primaries
        Map<GridServiceAgent, Integer> highPrimaries = new HashMap<>();
        boolean unbalanced = false;
        for (GridServiceAgent gsa : gsas) {
            int primaries = getInstanceCount(processingUnit, gsa);

            if (primaries > instancesPerAgent) {
                highPrimaries.put(gsa, primaries);
            }
            if (primaries > instancesPerAgent + 1) {
                unbalanced = true;
            }
        }

        if (!unbalanced && highPrimaries.size() == unbalancedNodes) {

            // balance backups
            logger.info(puName + " is balanced on primaries. Starting backup checking...");

            if (processingUnit.getType() == ProcessingUnitType.STATELESS) {
                return true;
            }
            return doRebalancingOfBackups(processingUnit, gsas);
        }
        return false;
    }

    private int getInstanceCount(ProcessingUnit processingUnit, GridServiceAgent gsa) {
        int primaries;

        if (processingUnit.getType() == ProcessingUnitType.STATELESS) {
            primaries = listInstancesOnGSA(gsa).size();
        } else {
            primaries = listPrimariesOnGSA(gsa).size();
        }
        return primaries;
    }

    private boolean doRebalancingOfBackups(ProcessingUnit processingUnit, List<GridServiceAgent> gsas) {

        //how many unbalanced nodes can be?
        int initialPartitionCount = processingUnit.getPartitions().length;
        int instancesPerAgent = initialPartitionCount / gsas.size();
        int unbalancedNodes = initialPartitionCount % gsas.size();

        boolean unbalanced = false;

        for (GridServiceAgent gsa : gsas) {
            int backups = listBackupsOnGSA(gsa).size();

            if ((listPrimariesOnGSA(gsa).size() > instancesPerAgent) || (backups > instancesPerAgent + unbalancedNodes)) {
                // node contains more primaries
                unbalanced = true;
            }
        }

        if (!unbalanced) {
            logger.info(puName + " is balanced");
            return true;
        }
        return false;
    }

    private List<ProcessingUnitInstance> listPrimariesOnGSA(GridServiceAgent gsa) {
        List<ProcessingUnitInstance> primaries = new ArrayList<>();
        GridServiceContainers gscs = gsa.getMachine().getGridServiceContainers();
        gscs.waitFor(1, 2, TimeUnit.SECONDS);
        for (GridServiceContainer gsc : gscs.getContainers()){
            for (ProcessingUnitInstance pui : gsc.getProcessingUnitInstances(puName)){
                waitForInstanceInit(pui);
                if (pui.getSpaceInstance().getMode() == SpaceMode.PRIMARY){
                    primaries.add(pui);
                }
            }
        }
        return primaries;
    }

    private List<ProcessingUnitInstance> listBackupsOnGSA(GridServiceAgent gsa) {
        List<ProcessingUnitInstance> backups = new ArrayList<>();
        GridServiceContainers gscs = gsa.getMachine().getGridServiceContainers();
        gscs.waitFor(1, 2, TimeUnit.SECONDS);
        for (GridServiceContainer gsc : gscs.getContainers()){
            for (ProcessingUnitInstance pui : gsc.getProcessingUnitInstances(puName)){
                waitForInstanceInit(pui);
                if (pui.getSpaceInstance().getMode() == SpaceMode.BACKUP){
                    backups.add(pui);
                }
            }
        }
        return backups;
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
    // after instance relocated it should be initialized before starting next iteration
    private void waitForInstanceInit(ProcessingUnitInstance pui) {
        while (pui.getSpaceInstance().getMode() == SpaceMode.NONE){}
    }


}
