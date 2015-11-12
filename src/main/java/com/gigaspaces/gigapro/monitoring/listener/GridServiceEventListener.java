package com.gigaspaces.gigapro.monitoring.listener;

import com.gigaspaces.gigapro.monitoring.RebalancingTask;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;

import java.util.concurrent.Executor;

public class GridServiceEventListener implements GridServiceAgentAddedEventListener, GridServiceAgentRemovedEventListener{

    private Admin admin;

    private Executor executor;

    public GridServiceEventListener(Admin admin, Executor executor) {
        this.admin = admin;
        this.executor = executor;
    }

    @Override
    public void gridServiceAgentAdded(GridServiceAgent gridServiceAgent) {
        System.out.println("GSA added, starting rebalancing...");
        executor.execute(new RebalancingTask(admin));
    }

    @Override
    public void gridServiceAgentRemoved(GridServiceAgent gridServiceAgent) {
        System.out.println("GSA removed, starting rebalancing...");
        executor.execute(new RebalancingTask(admin));
    }
}
