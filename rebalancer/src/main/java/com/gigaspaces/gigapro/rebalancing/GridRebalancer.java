package com.gigaspaces.gigapro.rebalancing;

import com.gigaspaces.gigapro.rebalancing.listener.GridServiceEventListener;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.core.space.mode.PostPrimary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//import javax.annotation.PostConstruct;
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

        // rebalance grid first time
        logger.info("Rebalancing: going to rebalance grid first time");
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new RebalancingTask(admin));

        // rebalance grid each time GSA is added
        GridServiceEventListener gridServiceEventListener = new GridServiceEventListener(admin, executor);
        admin.getGridServiceAgents().getGridServiceAgentAdded().add(gridServiceEventListener);
        admin.getGridServiceAgents().getGridServiceAgentRemoved().add(gridServiceEventListener);
    }

}
