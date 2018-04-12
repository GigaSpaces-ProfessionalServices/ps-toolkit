package com.gigaspaces.gigapro.rebalancing.listener;

import com.gigaspaces.gigapro.rebalancing.DryRunRebalancer;
import com.gigaspaces.gigapro.rebalancing.ZoneUtils;
import com.gigaspaces.gigapro.rebalancing.gsa.rebalancer.RebalancingTask;
import com.gigaspaces.gigapro.rebalancing.gsc.rebalancer.RebalancingWithinAgentTask;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventListener;
import org.openspaces.admin.pu.ProcessingUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class GridServiceEventListener implements GridServiceAgentAddedEventListener, GridServiceAgentRemovedEventListener,
        GridServiceContainerAddedEventListener, GridServiceContainerRemovedEventListener {

    private static Logger logger = LoggerFactory.getLogger(GridServiceEventListener.class);
    private static AtomicBoolean ENABLE = new AtomicBoolean(true);

    private static Admin admin;

    private static Executor executor;

    public GridServiceEventListener(Admin admin, Executor executor) {
        GridServiceEventListener.admin = admin;
        GridServiceEventListener.executor = executor;
    }

    public static void enable() {
        ENABLE.compareAndSet(false, true);
        // wait processing unit deployment
        waitForPU();
        executor.execute(new RebalancingTask(admin));
    }

    public static void disable() {
        ENABLE.compareAndSet(true, false);
    }

    public static void rebalance() {
        waitForPU();
        executor.execute(new RebalancingTask(admin));
    }

    public static boolean isGridBalanced() {
        boolean balanced = true;
        for (ProcessingUnit pu : admin.getProcessingUnits()) {
            balanced = new DryRunRebalancer(pu.getName()).doDryRunRebalancing(pu,
                    ZoneUtils.sortGridServiceAgentsByZones(admin, pu.getRequiredContainerZones().getZones()));

            if (!balanced) break;
        }
        return balanced;
    }

    private static void waitForPU() {
        while(admin.getProcessingUnits().getSize() == 0) {}
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

    @Override
    public void gridServiceContainerAdded(GridServiceContainer gridServiceContainer) {
        if (ENABLE.get()) {
            logger.info("GSC added, starting rebalancing...");
            executor.execute(new RebalancingWithinAgentTask(admin));
        }
    }

    @Override
    public void gridServiceContainerRemoved(GridServiceContainer gridServiceContainer) {
        if (ENABLE.get()) {
            logger.info("GSC removed, starting rebalancing...");
            executor.execute(new RebalancingWithinAgentTask(admin));
        }
    }
}
