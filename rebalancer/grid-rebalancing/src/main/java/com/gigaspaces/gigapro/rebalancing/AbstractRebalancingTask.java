package com.gigaspaces.gigapro.rebalancing;

import org.openspaces.admin.Admin;

/**
 * @author Denys_Novikov
 * Date: 31.05.2018
 */
public abstract class AbstractRebalancingTask implements Runnable {

    protected Admin admin;

    public AbstractRebalancingTask(Admin admin) {
        this.admin = admin;
    }

}
