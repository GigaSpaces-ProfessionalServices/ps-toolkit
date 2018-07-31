package com.gigaspaces.gigapro.rebalancing.gsa.rebalancer;

import com.gigaspaces.gigapro.rebalancing.AbstractRebalancer;
import com.gigaspaces.gigapro.rebalancing.ZoneUtils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ProcessingUnitRebalancer extends AbstractRebalancer {

    private static Logger logger = LoggerFactory.getLogger(ProcessingUnitRebalancer.class);
    
    protected String puName;

    public ProcessingUnitRebalancer(Admin admin, String puName) {
        super(admin);
        this.puName = puName;
    }

    public void rebalance(AtomicBoolean inProgress){
        //get PU
        ProcessingUnit processingUnit = getProcessingUnit(puName);

        // get GSAa with zone
        List<GridServiceAgent> gsas = ZoneUtils.sortGridServiceAgentsByZones(admin, processingUnit.getRequiredContainerZones().getZones());

        doRebalancing(processingUnit, gsas);
       
    }

    protected abstract void doRebalancing(ProcessingUnit processingUnit, List<GridServiceAgent> gsas);

}
