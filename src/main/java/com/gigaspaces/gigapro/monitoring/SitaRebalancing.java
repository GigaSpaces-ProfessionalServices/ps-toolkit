package com.gigaspaces.gigapro.monitoring;

import com.gigaspaces.gigapro.monitoring.listener.GridServiceEventListener;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.springframework.beans.factory.InitializingBean;

public class SitaRebalancing implements InitializingBean {

    private String locators;

    @Override
    public void afterPropertiesSet() throws Exception {
        Admin admin = new AdminFactory().createAdmin();
        admin.getMachines().waitFor(1);

        // rebalance grid first time
        new GridRebalancer(admin).rebalanceGrid();

        // rebalance grid each time GSA is added
        GridServiceEventListener gridServiceEventListener = new GridServiceEventListener(admin);
        admin.getGridServiceAgents().getGridServiceAgentAdded().add(gridServiceEventListener);
        admin.getGridServiceAgents().getGridServiceAgentRemoved().add(gridServiceEventListener);
    }

}
