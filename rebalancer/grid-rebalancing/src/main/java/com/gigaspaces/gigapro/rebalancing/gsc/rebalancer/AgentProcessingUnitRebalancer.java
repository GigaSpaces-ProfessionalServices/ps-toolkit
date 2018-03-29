package com.gigaspaces.gigapro.rebalancing.gsc.rebalancer;

import com.gigaspaces.gigapro.rebalancing.AbstractRebalancer;
import com.gigaspaces.gigapro.rebalancing.ZoneUtils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;

import java.util.List;

/**
 * @author Denys_Novikov
 * Date: 05.04.2018
 */
public abstract class AgentProcessingUnitRebalancer extends AbstractRebalancer {

    protected String puName;

    public AgentProcessingUnitRebalancer(Admin admin, String puName) {
        super(admin);
        this.puName = puName;
    }


    public void rebalanceContainers() {
        //get PU
        ProcessingUnit processingUnit = getProcessingUnit(puName);

        // get GSAa with zone
        List<GridServiceAgent> gsas = ZoneUtils.sortGridServiceAgentsByZones(admin, processingUnit.getRequiredContainerZones().getZones());

        for (GridServiceAgent gsa : gsas) {
            doRebalancingBetweenContainers(gsa);
        }
    }

    /**
     * rebalances processing unit between GSCs within single GSA
     * @param gsa
     */
    protected abstract void doRebalancingBetweenContainers(GridServiceAgent gsa);
}
