package com.gigaspaces.gigapro.rebalancing.listener;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Denys_Novikov
 * Date: 13.03.2018
 */
public class ZooKeeperUtils {

    private static final String MANAGER_HOSTS = "XAP_MANAGER_SERVERS";
    private static final String NODE_PATH = "/state";
    private static Logger logger = LoggerFactory.getLogger(ZooKeeperUtils.class);
    private static final Watcher WATCHER_STUB = new WatcherImpl();


    public static void persistState(RebalancerState state) {
        String hostnamesAsString = System.getenv(MANAGER_HOSTS);

        if (hostnamesAsString != null && !hostnamesAsString.isEmpty()) {

            ZooKeeper zk = null;
            try {
                zk = getZooKeeper(hostnamesAsString);
                if (zk.exists(NODE_PATH, false) != null) {
                    zk.delete(NODE_PATH, -1);
                }
                zk.create(NODE_PATH, state.name().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

                logger.warn(String.format("Rebalancer state persisted as %s", state.name()));
            } catch (KeeperException | InterruptedException | IOException e) {
                logger.warn("Failed to persist state to ZooKeeper", e.getMessage());
            } finally {
                if (zk != null) {
                    try {
                        zk.close();
                    } catch (InterruptedException e) {
                        logger.warn("Failed to close ZooKeeper", e.getMessage());
                    }
                }
            }
        } else {
            logger.warn("Hosts not found, state won't be persisted!");
        }
    }

    public static RebalancerState getState() {
        String hostnamesAsString = System.getenv(MANAGER_HOSTS);

        if (hostnamesAsString != null && !hostnamesAsString.isEmpty()) {

            ZooKeeper zk = null;
            try {
                zk = getZooKeeper(hostnamesAsString);
                if (zk.exists(NODE_PATH, false) != null) {
                    byte[] resultState = zk.getData(NODE_PATH, false, null);
                    if (resultState != null && resultState.length != 0) {
                        return RebalancerState.valueOf(new String(resultState).toUpperCase());
                    }
                }

            } catch (KeeperException | InterruptedException | IOException e) {
                logger.warn("Failed to persist state to ZooKeeper", e.getMessage());
            } finally {
                if (zk != null) {
                    try {
                        zk.close();
                    } catch (InterruptedException e) {
                        logger.warn("Failed to close ZooKeeper", e.getMessage());
                    }
                }
            }
        } else {
            logger.warn("Hosts not found, state won't be persisted!");
        }
        return null;
    }

    private static ZooKeeper getZooKeeper(String hostnamesAsString) throws IOException {
        String zkHostnames = Arrays.stream(hostnamesAsString.split(","))
                .map(hostname -> String.format("%s:2181", hostname)).collect(Collectors.joining(","));

        return new ZooKeeper(zkHostnames, 3000, WATCHER_STUB);
    }


    private static class WatcherImpl implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            // ignore event
        }
    }
}
