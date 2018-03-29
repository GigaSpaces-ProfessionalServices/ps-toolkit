package com.gigaspaces.gigapro.rebalancing.listener;

import com.gigaspaces.gigapro.SingleRebalanceEvent;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Denys_Novikov
 * Date: 28.03.2018
 */
@EventDriven
@Polling
public class SingleRebalanceEventListener {

    private static Logger logger = LoggerFactory.getLogger(SingleRebalanceEventListener.class);

    @EventTemplate
    SingleRebalanceEvent unprocessedData() {
        return new SingleRebalanceEvent();
    }

    @SpaceDataEvent
    public void eventListener(SingleRebalanceEvent event) {
        logger.info("Single rebalance event received");
        GridServiceEventListener.rebalance();
        logger.info("Grid rebalancing initiated");
    }
}
