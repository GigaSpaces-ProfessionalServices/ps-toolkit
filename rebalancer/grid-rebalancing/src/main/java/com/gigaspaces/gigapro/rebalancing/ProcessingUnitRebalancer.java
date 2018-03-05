package com.gigaspaces.gigapro.rebalancing;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;

import java.util.*;

public abstract class ProcessingUnitRebalancer {

    protected int MAX_REBALANCING_COUNT = 10;

    protected Admin admin;

    protected String puName;

    public ProcessingUnitRebalancer(Admin admin, String puName) {
        this.admin = admin;
        this.puName = puName;
    }

    public void rebalance(){
        //get PU
        ProcessingUnit processingUnit = getProcessingUnit(puName);

        // get GSAa with zone
        List<GridServiceAgent> gsas = getGridServiceAgents(processingUnit.getRequiredContainerZones().getZones());

        doRebalancing(processingUnit, gsas);
    }

    protected abstract void doRebalancing(ProcessingUnit processingUnit, List<GridServiceAgent> gsas);

    protected Map<GridServiceAgent, List<GridServiceContainer>> buildEmptyContainersMap(List<GridServiceAgent> gsas){
        Map<GridServiceAgent, List<GridServiceContainer>> result = new HashMap<>();
        for (GridServiceAgent gsa : gsas){
            for (GridServiceContainer gsc : gsa.getMachine().getGridServiceContainers()){
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

    private ProcessingUnit getProcessingUnit(String puName) {
        return admin.getProcessingUnits().getProcessingUnit(puName);
    }

    private List<GridServiceAgent> getGridServiceAgents(Set<String> zones) {
        List<GridServiceAgent> result = new ArrayList<>();
        GridServiceAgents gridServiceAgents = admin.getGridServiceAgents();
        gridServiceAgents.waitFor(1);

        // if pu zones is empty, any agent can service it
        if (zones == null || zones.isEmpty()) {
            return Arrays.asList(gridServiceAgents.getAgents());
        }

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

}
