package com.gigaspaces.gigapro.rebalancing.listener;

import com.gigaspaces.gigapro.GridStateEvent;
import com.j_spaces.core.IJSpace;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
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
public class GridStateEventListener {

    private static Logger logger = LoggerFactory.getLogger(GridStateEventListener.class);

    @EventTemplate
    GridStateEvent unprocessedData() {
        return new GridStateEvent();
    }

    @SpaceDataEvent
    public void eventListener(GridStateEvent event) {
        logger.info("Grid state event received. Checking state...");

        IJSpace space = new UrlSpaceConfigurer("jini://*/*/controllerSpace").space();
        GigaSpace gigaSpace = new GigaSpaceConfigurer(space).gigaSpace();

        boolean balanced = GridServiceEventListener.isGridBalanced();

        logger.info("Grid state balanced = " + balanced);
        event.setProcessed(true);
        event.setBalanced(balanced);

        gigaSpace.write(event);
        logger.info("Grid state returned");
    }

}
