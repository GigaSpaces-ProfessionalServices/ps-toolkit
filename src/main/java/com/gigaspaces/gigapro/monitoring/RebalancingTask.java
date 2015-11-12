package com.gigaspaces.gigapro.monitoring;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitType;
import org.openspaces.admin.pu.ProcessingUnits;

public class RebalancingTask implements Runnable{

    private Admin admin;

    public RebalancingTask(Admin admin) {
        this.admin = admin;
    }

    @Override
    public void run() {
        ProcessingUnits processingUnits = admin.getProcessingUnits();
        checkGridState();
        for (ProcessingUnit processingUnit : processingUnits){
            ProcessingUnitType puType = processingUnit.getType();
            if (puType == ProcessingUnitType.STATEFUL){
                new StatefulProcessingUnitRebalancer(admin, processingUnit.getName()).rebalance();
            }
        }
    }

    private void checkGridState() {
        checkTwoGSAsArentDeployedOnOneMachine();
    }

    private void checkTwoGSAsArentDeployedOnOneMachine() {
        admin.getGridServiceAgents().waitForAtLeastOne();
        for (GridServiceAgent gsa : admin.getGridServiceAgents()){
            if (singleZoneGSA(gsa)){
                for (GridServiceAgent gsaOnTheSameMachine : gsa.getMachine().getGridServiceAgents()){
                    if (notTheSameGSA(gsa, gsaOnTheSameMachine) && singleZoneGSA(gsaOnTheSameMachine)){
                        if (gsaOnTheSameMachine.getExactZones().isSatisfiedBy(gsa.getExactZones())){
                            throw new RuntimeException("Two GSAs with the same zone are running on one machine");
                        }
                    }
                }
            }
        }
    }

    private boolean notTheSameGSA(GridServiceAgent gsa, GridServiceAgent gsaOnTheSameMachine) {
        return !gsa.getUid().equals(gsaOnTheSameMachine.getUid());
    }

    private boolean singleZoneGSA(GridServiceAgent gsa) {
        return gsa.getExactZones().getZones().size() == 1;
    }

}
