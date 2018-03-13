package com.gigaspaces.gigapro.rebalancing.listener;

import com.gigaspaces.gigapro.RebalancerStartEvent;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Denys_Novikov
 * Date: 23.02.2018
 */
@EventDriven
@Polling
public class RebalancerStartEventListener {

    private static Logger logger = LoggerFactory.getLogger(RebalancerStartEventListener.class);

    @EventTemplate
    RebalancerStartEvent unprocessedData() {
        return new RebalancerStartEvent();
    }

    @SpaceDataEvent
    public void eventListener(RebalancerStartEvent event) {
        logger.info("Start event received!!!");
        GridServiceEventListener.enable();
        logger.info("Grid rebalancer enabled");
        ZooKeeperUtils.persistState(RebalancerState.ON);
    }
}
