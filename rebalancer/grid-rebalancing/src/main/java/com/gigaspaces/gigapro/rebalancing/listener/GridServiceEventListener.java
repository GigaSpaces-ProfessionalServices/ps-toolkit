package com.gigaspaces.gigapro.rebalancing.listener;

import com.gigaspaces.gigapro.rebalancing.AbstractRebalancingTask;
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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class GridServiceEventListener implements GridServiceAgentAddedEventListener, GridServiceAgentRemovedEventListener,
        GridServiceContainerAddedEventListener, GridServiceContainerRemovedEventListener {

    private static Logger logger = LoggerFactory.getLogger(GridServiceEventListener.class);
    private static AtomicBoolean ENABLE = new AtomicBoolean(true);
    private static AtomicBoolean IN_PROGRESS = new AtomicBoolean(false);

    private static BlockingQueue<AbstractRebalancingTask> GSA_TASK_QUEUE = new ArrayBlockingQueue<>(10);
    private static BlockingQueue<AbstractRebalancingTask> GSC_TASK_QUEUE = new ArrayBlockingQueue<>(10);

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
        if (!IN_PROGRESS.get()) {
            markRebalanceInProgress();
            executor.execute(new RebalancingTask(admin, IN_PROGRESS));
        }
    }

    private static void markRebalanceInProgress() {
        IN_PROGRESS.compareAndSet(false, true);
    }

    public static void disable() {
        ENABLE.compareAndSet(true, false);
    }

    public static void rebalance() {
        waitForPU();
        if (!IN_PROGRESS.get()) {
            markRebalanceInProgress();
            executor.execute(new RebalancingTask(admin, IN_PROGRESS));
        }
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
        insertTaskToQueue(GSA_TASK_QUEUE, new RebalancingTask(admin, IN_PROGRESS));
        if (ENABLE.get() && !IN_PROGRESS.get()) {

            if (gridBalanced()) return;

            logger.info("GSA added, starting rebalancing...");
            markRebalanceInProgress();
            if (!GSA_TASK_QUEUE.isEmpty())
                executor.execute(GSA_TASK_QUEUE.poll());
        }
    }

    @Override
    public void gridServiceAgentRemoved(GridServiceAgent gridServiceAgent) {
        insertTaskToQueue(GSA_TASK_QUEUE, new RebalancingTask(admin, IN_PROGRESS));
        if (ENABLE.get() && !IN_PROGRESS.get()) {

            if (gridBalanced()) return;

            logger.info("GSA removed, starting rebalancing...");
            markRebalanceInProgress();
            if (!GSA_TASK_QUEUE.isEmpty())
                executor.execute(GSA_TASK_QUEUE.poll());
        }
    }

    @Override
    public void gridServiceContainerAdded(GridServiceContainer gridServiceContainer) {
        insertTaskToQueue(GSC_TASK_QUEUE, new RebalancingWithinAgentTask(admin, IN_PROGRESS));
        if (ENABLE.get() && !IN_PROGRESS.get()){

            if (agentBalanced()) return;

            logger.info("GSC added, starting rebalancing...");
            markRebalanceInProgress();
            if (!GSC_TASK_QUEUE.isEmpty())
                executor.execute(GSC_TASK_QUEUE.poll());
        }
    }

    @Override
    public void gridServiceContainerRemoved(GridServiceContainer gridServiceContainer) {
        insertTaskToQueue(GSC_TASK_QUEUE, new RebalancingWithinAgentTask(admin, IN_PROGRESS));
        if (ENABLE.get() && !IN_PROGRESS.get()){

            if (agentBalanced()) return;

            logger.info("GSC removed, starting rebalancing...");
            markRebalanceInProgress();
            if (!GSC_TASK_QUEUE.isEmpty())
                executor.execute(GSC_TASK_QUEUE.poll());
        }
    }

    private boolean gridBalanced() {
        if (isGridBalanced()) {
            logger.info("Grid is balanced, queue will be cleared");
            GSA_TASK_QUEUE.clear();
            return true;
        }
        return false;
    }

    private boolean agentBalanced() {
        if (isGridBalanced()) {
            logger.info("Agent is balanced, queue will be cleared");
            GSC_TASK_QUEUE.clear();
            return true;
        }
        return false;
    }

    private void insertTaskToQueue(BlockingQueue<AbstractRebalancingTask>  queue, AbstractRebalancingTask task) {
        try {
            if (!queue.offer(task)) {
                logger.info("No space in queue, waiting ");
                queue.put(task);
            }
        } catch (Exception e) {
            logger.error("Exception while inserting to queue: ", e);
        }
    }
}
