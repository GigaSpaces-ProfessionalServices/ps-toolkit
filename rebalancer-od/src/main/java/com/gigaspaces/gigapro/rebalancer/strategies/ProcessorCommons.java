package com.gigaspaces.gigapro.rebalancer.strategies;

import com.gigaspaces.gigapro.rebalancer.Constants;
import com.gigaspaces.gigapro.rebalancer.config.Configuration;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.lastIndexOf;

public class ProcessorCommons {

    private static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    /**
     * Relocates excess processing instances to empty GSCs
     *
     * @param availableContainers empty GSCs which excess instances will be relocated to
     * @param excessInstances excess instances to be relocated
     */
    public static void relocateExcessInstances(Set<GridServiceContainer> availableContainers, Set<ProcessingUnitInstance> excessInstances, Configuration configuration) throws RuntimeException {
        if (isEmpty(availableContainers) || isEmpty(excessInstances)) {
            logger.info("No containers and/or no processing units to relocate.");
            return;
        }

        logger.info("Starting relocation...");
        Iterator<ProcessingUnitInstance> instanceIterator = excessInstances.iterator();
        Iterator<GridServiceContainer> containerIterator = availableContainers.iterator();

        while (instanceIterator.hasNext()) {
            ProcessingUnitInstance processingUnit = instanceIterator.next();

            if (containerIterator.hasNext()) {
                GridServiceContainer targetContainer = containerIterator.next();
                logger.info(String.format("Relocating processing unit [%s] to container [%s].", processingUnit.getUid(), targetContainer.getUid()));

                processingUnit.relocateAndWait(targetContainer, configuration.getTimeout(), TimeUnit.MILLISECONDS);
            } else {
                throw new RuntimeException("No containers are available to finish relocation.");
            }
        }
    }

    /**
     * @param agent GSA to find processing instances in
     * @return list of all processing instances in agent
     */
    public static List<ProcessingUnitInstance> findInstances(GridServiceAgent agent, String processingUnitName) {
        return stream(agent.getGridServiceContainers().getContainers()).flatMap(c -> stream(c.getProcessingUnitInstances(processingUnitName))).collect(toList());
    }

    /**
     * @param agent GSA to find empty GSCs in
     * @return list of empty GSCs in agent
     */
    public static List<GridServiceContainer> findEmptyContainers(GridServiceAgent agent) {
        return stream(agent.getGridServiceContainers().getContainers()).filter(c -> c.getProcessingUnitInstances().length == 0).collect(toList());
    }

    /**
     * @param gridServiceAgents GSAs to find empty GSCs in
     * @return list of empty GSCs in gridServiceAgents
     */
    public static List<GridServiceContainer> findAllEmptyContainers(List<GridServiceAgent> gridServiceAgents) {
        return findAllContainers(gridServiceAgents).stream().filter(c -> c.getProcessingUnitInstances().length == 0).collect(toList());
    }

    /**
     * @param gridServiceAgents GSAs to find GSCs in
     * @return list of all GSCs in gridServiceAgents
     */
    public static List<GridServiceContainer> findAllContainers(List<GridServiceAgent> gridServiceAgents) {
        return gridServiceAgents.stream().flatMap(a -> stream(a.getGridServiceContainers().getContainers())).collect(toList());
    }

    /**
     * Check if instance is primary
     *
     * @param instance to check
     * @return true if instance is primary, false if instance is backup
     */
    public static boolean isPrimary(ProcessingUnitInstance instance) {
        return instance.getBackupId() == 0;
    }

    /**
     * Check if instance is backup
     *
     * @param instance to check
     * @return true if instance is backup, false if instance is primary
     */
    public static boolean isBackup(ProcessingUnitInstance instance) {
        return !isPrimary(instance);
    }

    /**
     * This method returns the name of the instance ignoring "[number]" in the end. <br><br>
     *
     * e.g. name = "space1.1 [2]"   result = "space1.1" <br><br>
     *
     * @param instance processing instance
     * @return Name of the instance
     */
    public static String getInstanceName(ProcessingUnitInstance instance) {
        return instance.getProcessingUnitInstanceName().substring(0, lastIndexOf(instance.getProcessingUnitInstanceName(), "[")).trim();
    }
}
