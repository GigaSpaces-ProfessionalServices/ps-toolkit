package com.gigaspaces.gigapro.rebalancing.listener;

import com.gigaspaces.gigapro.rebalancing.RebalancingTask;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public class GridServiceEventListener implements GridServiceAgentAddedEventListener, GridServiceAgentRemovedEventListener{

    private static Logger logger = LoggerFactory.getLogger(GridServiceEventListener.class);

    private Admin admin;

    private Executor executor;

    public GridServiceEventListener(Admin admin, Executor executor) {
        this.admin = admin;
        this.executor = executor;
    }

    @Override
    public void gridServiceAgentAdded(GridServiceAgent gridServiceAgent) {
        logger.info("GSA added, starting rebalancing...");
        executor.execute(new RebalancingTask(admin));
    }

    @Override
    public void gridServiceAgentRemoved(GridServiceAgent gridServiceAgent) {
        logger.info("GSA removed, starting rebalancing...");
        executor.execute(new RebalancingTask(admin));
    }
}
