package com.gigaspaces.gigapro.monitoring;

import com.gigaspaces.gigapro.monitoring.listener.GridServiceEventListener;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GridRebalancer implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Rebalancing: creating admin");
        Admin admin = new AdminFactory().createAdmin();
        System.out.println("Rebalancing: admin created");
        admin.getMachines().waitFor(1);
        System.out.println("Rebalancing: one machine appeared");

        // rebalance grid first time
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new RebalancingTask(admin));

        // rebalance grid each time GSA is added
        GridServiceEventListener gridServiceEventListener = new GridServiceEventListener(admin, executor);
        admin.getGridServiceAgents().getGridServiceAgentAdded().add(gridServiceEventListener);
        admin.getGridServiceAgents().getGridServiceAgentRemoved().add(gridServiceEventListener);
    }

}
