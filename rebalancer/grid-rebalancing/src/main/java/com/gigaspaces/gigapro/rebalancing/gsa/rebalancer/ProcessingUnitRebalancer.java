package com.gigaspaces.gigapro.rebalancing.gsa.rebalancer;

import com.gigaspaces.gigapro.rebalancing.AbstractRebalancer;
import com.gigaspaces.gigapro.rebalancing.ZoneUtils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;

import java.util.List;

public abstract class ProcessingUnitRebalancer extends AbstractRebalancer {

    protected String puName;

    public ProcessingUnitRebalancer(Admin admin, String puName) {
        super(admin);
        this.puName = puName;
    }

    public void rebalance(){
        //get PU
        ProcessingUnit processingUnit = getProcessingUnit(puName);

        // get GSAa with zone
        List<GridServiceAgent> gsas = ZoneUtils.sortGridServiceAgentsByZones(admin, processingUnit.getRequiredContainerZones().getZones());

        doRebalancing(processingUnit, gsas);
    }

    protected abstract void doRebalancing(ProcessingUnit processingUnit, List<GridServiceAgent> gsas);

}
