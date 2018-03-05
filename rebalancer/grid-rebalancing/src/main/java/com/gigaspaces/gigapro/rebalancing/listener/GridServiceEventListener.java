package com.gigaspaces.gigapro.rebalancing.listener;

import com.gigaspaces.gigapro.rebalancing.RebalancingTask;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class GridServiceEventListener implements GridServiceAgentAddedEventListener, GridServiceAgentRemovedEventListener{

    private static Logger logger = LoggerFactory.getLogger(GridServiceEventListener.class);
    private static AtomicBoolean ENABLE = new AtomicBoolean(true);

    private Admin admin;

    private Executor executor;

    public GridServiceEventListener(Admin admin, Executor executor) {
        this.admin = admin;
        this.executor = executor;
    }

    public static void enable() {
        ENABLE.compareAndSet(false, true);
    }

    public static void disable() {
        ENABLE.compareAndSet(true, false);
    }

    @Override
    public void gridServiceAgentAdded(GridServiceAgent gridServiceAgent) {
        if (ENABLE.get()) {
            logger.info("GSA added, starting rebalancing...");
            executor.execute(new RebalancingTask(admin));
        }
    }

    @Override
    public void gridServiceAgentRemoved(GridServiceAgent gridServiceAgent) {
        if (ENABLE.get()) {
            logger.info("GSA removed, starting rebalancing...");
            executor.execute(new RebalancingTask(admin));
        }
    }
}
