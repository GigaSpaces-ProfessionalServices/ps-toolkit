package com.gigaspaces.gigapro.monitoring;

import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;

public class GridRebalancer {

    private Admin admin;

    public GridRebalancer(Admin admin) {
        this.admin = admin;
    }

    public void rebalanceGrid(){
        ProcessingUnits processingUnits = admin.getProcessingUnits();
        for (ProcessingUnit processingUnit : processingUnits){
            new ProcessingUnitRebalancer(admin, processingUnit.getName()).rebalance();
        }
    }
}
