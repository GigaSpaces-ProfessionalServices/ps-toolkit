package com.gigaspaces.gigapro.rebalancing.gsa.rebalancer;

import com.gigaspaces.gigapro.rebalancing.AbstractRebalancer;
import com.gigaspaces.gigapro.rebalancing.ZoneUtils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ProcessingUnitRebalancer extends AbstractRebalancer {

    protected String puName;

    public ProcessingUnitRebalancer(Admin admin, String puName) {
        super(admin);
        this.puName = puName;
    }

    public void rebalance(Collection<String> agentsInRebalance){
        //get PU
        ProcessingUnit processingUnit = getProcessingUnit(puName);

        // get GSAa with zone
        List<GridServiceAgent> gsas = ZoneUtils.sortGridServiceAgentsByZones(admin, processingUnit.getRequiredContainerZones().getZones());

        doRebalancing(processingUnit, gsas);

        // remove rebalanced agents
        if (agentsInRebalance != null && !agentsInRebalance.isEmpty())
            agentsInRebalance.removeAll(gsas.stream().map(gsa -> gsa.getUid()).collect(Collectors.toList()));
    }

    protected abstract void doRebalancing(ProcessingUnit processingUnit, List<GridServiceAgent> gsas);

}
