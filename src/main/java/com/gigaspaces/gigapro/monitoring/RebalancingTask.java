package com.gigaspaces.gigapro.monitoring;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;

public class RebalancingTask implements Runnable{

    private Admin admin;

    public RebalancingTask(Admin admin) {
        this.admin = admin;
    }

    @Override
    public void run() {
        checkGridState();
        ProcessingUnits processingUnits = admin.getProcessingUnits();
        for (ProcessingUnit processingUnit : processingUnits){
            new ProcessingUnitRebalancer(admin, processingUnit.getName()).rebalance();
        }
    }

    private void checkGridState() {
        checkTwoGSAsArentDeployedOnOneMachine();
    }

    private void checkTwoGSAsArentDeployedOnOneMachine() {
        for (GridServiceAgent gsa : admin.getGridServiceAgents()){
            if (singleZoneGSA(gsa)){
                for (GridServiceAgent gsaOnTheSameMachine : gsa.getMachine().getGridServiceAgents()){
                    if (singleZoneGSA(gsaOnTheSameMachine)){
                        if (gsaOnTheSameMachine.getExactZones().isSatisfiedBy(gsa.getExactZones())){
                            throw new RuntimeException("Two GSAs with the same zone are running on one machine");
                        }
                    }
                }
            }
        }
    }

    private boolean singleZoneGSA(GridServiceAgent gsa) {
        return gsa.getExactZones().getZones().size() == 1;
    }

}
