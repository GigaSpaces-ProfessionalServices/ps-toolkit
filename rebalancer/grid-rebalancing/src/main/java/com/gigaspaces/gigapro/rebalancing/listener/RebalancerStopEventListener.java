package com.gigaspaces.gigapro.rebalancing.listener;

import com.gigaspaces.gigapro.RebalancerStopEvent;
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
public class RebalancerStopEventListener {

    private static Logger logger = LoggerFactory.getLogger(RebalancerStopEventListener.class);

    @EventTemplate
    RebalancerStopEvent unprocessedData() {
        return new RebalancerStopEvent();
    }

    @SpaceDataEvent
    public void eventListener(RebalancerStopEvent event) {
        logger.info("Stop event received");
        GridServiceEventListener.disable();
        logger.info("Grid rebalancer disabled");
    }
}
