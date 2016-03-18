package com.gigaspaces.gigapro.rebalancer.strategies;

import com.gigaspaces.gigapro.rebalancer.Constants;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.pu.ProcessingUnit;

import java.util.List;
import java.util.logging.Logger;

public class ProcessorWithBackup implements BalancerStrategy {
    private Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    @Override
    public void balance(ProcessingUnit targetPu, List<GridServiceAgent> gridServiceAgents) throws Exception {

    }
}

