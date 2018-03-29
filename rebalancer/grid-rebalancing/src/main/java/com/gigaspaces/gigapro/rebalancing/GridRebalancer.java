package com.gigaspaces.gigapro.rebalancing;

import com.gigaspaces.gigapro.rebalancing.listener.GridServiceEventListener;
import com.gigaspaces.gigapro.rebalancing.listener.RebalancerState;
import com.gigaspaces.gigapro.rebalancing.listener.ZooKeeperUtils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GridRebalancer {

    private static Logger logger = LoggerFactory.getLogger(GridRebalancer.class);

    public void init() throws Exception {
        logger.info("Rebalancing: creating admin");
        Admin admin = new AdminFactory().createAdmin();
        logger.info("Rebalancing: admin created");
        admin.getMachines().waitFor(1);
        logger.info("Rebalancing: one machine appeared");

        Executor executor = Executors.newSingleThreadExecutor();

        // rebalance grid each time GSA is added
        GridServiceEventListener gridServiceEventListener = new GridServiceEventListener(admin, executor);

        // check current state. if no persisted state found - rebalancer switched on by default
        RebalancerState currentState = ZooKeeperUtils.getState();
        logger.info(String.format("Setting state to %s", currentState));

        if (RebalancerState.OFF == currentState) {
            GridServiceEventListener.disable();
        } else {
            logger.info("Rebalancing: going to rebalance grid first time");
            GridServiceEventListener.enable();
        }

        admin.getGridServiceAgents().getGridServiceAgentAdded().add(gridServiceEventListener);
        admin.getGridServiceAgents().getGridServiceAgentRemoved().add(gridServiceEventListener);

        admin.getGridServiceContainers().getGridServiceContainerAdded().add(gridServiceEventListener);
        admin.getGridServiceContainers().getGridServiceContainerRemoved().add(gridServiceEventListener);
    }


}
