package com.gigaspaces.gigapro.rebalancer.strategies;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;

import java.util.List;

/**
 * Created by Skyler on 3/17/2016.
 */
public interface BalancerStrategy {
    void balance(ProcessingUnit targetPu, List<GridServiceAgent> gridServiceAgents) throws Exception;
}

