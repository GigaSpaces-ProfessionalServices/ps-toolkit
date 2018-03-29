package com.gigaspaces.gigapro.rebalancing;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Denys_Novikov
 * Date: 05.04.2018
 */
public abstract class AbstractRebalancer {

    protected int MAX_REBALANCING_COUNT = 10;

    protected Admin admin;

    protected AbstractRebalancer(Admin admin) {
        this.admin = admin;
    }

    protected Map<GridServiceAgent, List<GridServiceContainer>> buildEmptyContainersMap(List<GridServiceAgent> gsas){
        Map<GridServiceAgent, List<GridServiceContainer>> result = new HashMap<>();
        for (GridServiceAgent gsa : gsas){
            for (GridServiceContainer gsc : gsa.getMachine().getGridServiceContainers()){
                if (gsc.getProcessingUnitInstances().length == 0){
                    GridServiceAgent gridServiceAgent = gsc.getGridServiceAgent();
                    result.computeIfAbsent(gridServiceAgent, key -> new ArrayList<>()).add(gsc);
                }
            }
        }
        return result;
    }

    protected ProcessingUnit getProcessingUnit(String puName) {
        return admin.getProcessingUnits().getProcessingUnit(puName);
    }

    // after instance relocated it should be initialized before starting next iteration
    protected void waitForInstanceInit(ProcessingUnitInstance pui) {
        while (pui.getSpaceInstance().getMode() == SpaceMode.NONE){}
    }

    protected List<ProcessingUnitInstance> listPrimariesOnGSC(GridServiceContainer gsc, String puName) {
        List<ProcessingUnitInstance> primaries = new ArrayList<>();

        for (ProcessingUnitInstance pui : gsc.getProcessingUnitInstances(puName)) {
            waitForInstanceInit(pui);
            if (pui.getSpaceInstance().getMode() == SpaceMode.PRIMARY) {
                primaries.add(pui);
            }
        }
        return primaries;
    }

    protected List<ProcessingUnitInstance> listBackupsOnGSC(GridServiceContainer gsc, String puName) {
        List<ProcessingUnitInstance> primaries = new ArrayList<>();

        for (ProcessingUnitInstance pui : gsc.getProcessingUnitInstances(puName)) {
            waitForInstanceInit(pui);
            if (pui.getSpaceInstance().getMode() == SpaceMode.BACKUP) {
                primaries.add(pui);
            }
        }
        return primaries;
    }

    protected int getBackupPerAgentCount(int instancesPerAgent) {
        return instancesPerAgent + 1;
    }

}
