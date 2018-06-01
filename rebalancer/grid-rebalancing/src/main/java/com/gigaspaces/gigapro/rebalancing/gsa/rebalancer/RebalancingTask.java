package com.gigaspaces.gigapro.rebalancing.gsa.rebalancer;

import com.gigaspaces.gigapro.rebalancing.AbstractRebalancingTask;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitType;
import org.openspaces.admin.pu.ProcessingUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class RebalancingTask extends AbstractRebalancingTask {

    private static Logger logger = LoggerFactory.getLogger(RebalancingTask.class);

    private Collection<String> agentsInRebalance;

    public RebalancingTask(Admin admin, Collection<String> agentsInRebalance) {
        super(admin);
        this.agentsInRebalance = agentsInRebalance;
    }

    public RebalancingTask(Admin admin) {
        super(admin);
    }

    @Override
    public void run() {
        logger.info("Rebalancing started");
        ProcessingUnits processingUnits = admin.getProcessingUnits();
        checkGridState();
        logger.info("Rebalancing: " + processingUnits.getSize() + " PUs found");
        for (ProcessingUnit processingUnit : processingUnits){
            ProcessingUnitType puType = processingUnit.getType();
            if (processingUnit.getInstances().length > 1){
                if (puType == ProcessingUnitType.STATEFUL){
                    new StatefulProcessingUnitRebalancer(admin, processingUnit.getName()).rebalance(agentsInRebalance);
                } else {
                    new StatelessProcessingUnitRebalancer(admin, processingUnit.getName()).rebalance(agentsInRebalance);
                }
            } else {
                logger.info(String.format("ProcessingUnit %s has one instance and can't be rebalanced", processingUnit.getName()));
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
                    if (twoSingleZoneGSAsOnTheSameMachine(gsa, gsaOnTheSameMachine)){
                            throw new RuntimeException(String.format("Two GSAs with the same zone are running on one machine. Zone %s. UID-1 %s, UID-2 %s",
                                                        gsa.getExactZones().getZones().iterator().next(), gsa.getUid(), gsaOnTheSameMachine.getUid()));
                    }
                }
            }
        }
    }

    private boolean twoSingleZoneGSAsOnTheSameMachine(GridServiceAgent gsa, GridServiceAgent gsaOnTheSameMachine) {
        return notTheSameGSA(gsa, gsaOnTheSameMachine) && singleZoneGSA(gsaOnTheSameMachine) && gsaOnTheSameMachine.getExactZones().isSatisfiedBy(gsa.getExactZones());
    }

    private boolean notTheSameGSA(GridServiceAgent gsa, GridServiceAgent gsaOnTheSameMachine) {
        return !gsa.getUid().equals(gsaOnTheSameMachine.getUid());
    }

    private boolean singleZoneGSA(GridServiceAgent gsa) {
        return gsa.getExactZones().getZones().size() == 1;
    }

}
