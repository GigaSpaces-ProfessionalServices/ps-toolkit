package com.gigaspaces.gigapro.rebalancing;

import org.openspaces.admin.Admin;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Denys_Novikov
 * Date: 31.05.2018
 */
public abstract class AbstractRebalancingTask implements Runnable {

    protected Admin admin;

    protected AtomicBoolean inProgress;

    public AbstractRebalancingTask(Admin admin, AtomicBoolean inProgress) {
        this.admin = admin;
        this.inProgress = inProgress;
    }

}
