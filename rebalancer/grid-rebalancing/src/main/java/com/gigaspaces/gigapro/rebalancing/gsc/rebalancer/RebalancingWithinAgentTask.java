package com.gigaspaces.gigapro.rebalancing.gsc.rebalancer;

import com.gigaspaces.gigapro.rebalancing.AbstractRebalancingTask;
import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitType;
import org.openspaces.admin.pu.ProcessingUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Denys_Novikov
 * Date: 30.03.2018
 */
public class RebalancingWithinAgentTask extends AbstractRebalancingTask {

    private static Logger logger = LoggerFactory.getLogger(RebalancingWithinAgentTask.class);

    public RebalancingWithinAgentTask(Admin admin, AtomicBoolean inProgress) {
        super(admin, inProgress);
    }

    @Override
    public void run() {
        logger.info("************************* Rebalancing started **********************************");
        ProcessingUnits processingUnits = admin.getProcessingUnits();
        logger.info("Rebalancing: " + processingUnits.getSize() + " PUs found");
        
        for (ProcessingUnit processingUnit : processingUnits){
            ProcessingUnitType puType = processingUnit.getType();
            if (processingUnit.getInstances().length > 1){
                if (puType == ProcessingUnitType.STATEFUL){
                    new StatefulAgentProcessingUnitRebalancer(admin, processingUnit.getName()).rebalanceContainers(inProgress);
                } else {
                    new StatelessAgentProcessingUnitRebalancer(admin, processingUnit.getName()).rebalanceContainers(inProgress);
                }
            } else {
                logger.info(String.format("ProcessingUnit %s has one instance and can't be rebalanced", processingUnit.getName()));
            }
        }
        logger.info("*********************************** Rebalancer is complete *******************************");
        inProgress.compareAndSet(true, false);
    }

}
