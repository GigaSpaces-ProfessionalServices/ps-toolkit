package com.gigaspaces.gigapro.monitoring.listener;

import com.gigaspaces.gigapro.monitoring.GridRebalancer;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;

public class GridServiceEventListener implements GridServiceAgentAddedEventListener, GridServiceAgentRemovedEventListener{

    private Admin admin;

    public GridServiceEventListener(Admin admin) {
        this.admin = admin;
    }

    @Override
    public void gridServiceAgentAdded(GridServiceAgent gridServiceAgent) {
        new GridRebalancer(admin).rebalanceGrid();
    }

    @Override
    public void gridServiceAgentRemoved(GridServiceAgent gridServiceAgent) {
        new GridRebalancer(admin).rebalanceGrid();
    }
}
