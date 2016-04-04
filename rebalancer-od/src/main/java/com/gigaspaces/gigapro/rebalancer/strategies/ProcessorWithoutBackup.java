package com.gigaspaces.gigapro.rebalancer.strategies;

import com.gigaspaces.gigapro.rebalancer.Constants;
import com.gigaspaces.gigapro.rebalancer.config.Configuration;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.Arrays.stream;
import static java.util.Collections.addAll;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public class ProcessorWithoutBackup implements BalancerStrategy {
    private Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private Configuration configuration;

    public ProcessorWithoutBackup(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void balance(ProcessingUnit targetPu, List<GridServiceAgent> gridServiceAgents) throws Exception {
        logger.info("Using the backup-less rebalancing strategy.");
        final int totalMembers = targetPu.getPlannedNumberOfInstances();

        // The number of agents with an extra instance
        final int machinesWithExtraInstance = totalMembers % gridServiceAgents.size();

        // Number of instances on each machine (not including imbalance instances)
        final int evenInstanceCount = totalMembers / gridServiceAgents.size();

        // Corrects the upper bound number of processing unit on a machine.
        final int agentCeil = evenInstanceCount + (machinesWithExtraInstance > 0 ? 1 : 0);

        logger.info(String.format("Each GSA will host %s instance(s).", evenInstanceCount));

        if (machinesWithExtraInstance > 0)
            logger.info(String.format("%s GSAs will host %s instance(s).", machinesWithExtraInstance, (evenInstanceCount + 1)));

        Set<GridServiceContainer> availableContainers = new HashSet<>();
        Set<ProcessingUnitInstance> excessInstances = new HashSet<>();

        populateAgentCollections(gridServiceAgents, agentCeil, availableContainers, excessInstances);

        relocateExcessInstances(availableContainers, excessInstances);
    }

    /**
     * Searches for empty GSCs and excess processing instances in gridServiceAgents list and fills corresponding sets
     *
     * @param gridServiceAgents   list of GSAs
     * @param agentCeil           upper bound number of processing units per machine
     * @param availableContainers empty set for available GSCs (being filled in the method)
     * @param excessInstances     empty set for excess processing instances (being filled in the method)
     * @throws Exception
     */
    private void populateAgentCollections(List<GridServiceAgent> gridServiceAgents, int agentCeil, Set<GridServiceContainer> availableContainers, Set<ProcessingUnitInstance> excessInstances) throws Exception {
        for (GridServiceAgent agent : gridServiceAgents) {
            long agentPid = agent.getVirtualMachine().getDetails().getPid();
            List<GridServiceContainer> emptyContainers = findEmptyContainers(agent);
            List<ProcessingUnitInstance> agentInstances = findInstances(agent);
            int instanceCount = agentInstances.size();

            String operation = "NO ACTION";
            final int numberToAdd = Math.abs(agentCeil - instanceCount);
            if (instanceCount > agentCeil) {
                operation = "DECREMENT COUNT";
                addExcessToList(excessInstances, agentInstances, numberToAdd); // add excess processing instances from agentInstances to excessInstances
            } else if (instanceCount < agentCeil) {
                operation = "INCREMENT COUNT";
                addExcessToList(availableContainers, emptyContainers, numberToAdd); // adds empty GSCs from emptyContainers to availableContainers
            }

            logger.info(String.format("[ Process: %s, Managed Instances: %s, Operation: %s ]", agentPid, instanceCount, operation));
        }
    }

    /**
     * Adds numberToAdd items from source to dest. If source.size() < numberToAdd, then source.size() items will be added
     *
     * @param dest        destination Set
     * @param source      source list
     * @param numberToAdd max number of items to add from source to dest
     */
    private <T> void addExcessToList(Set<T> dest, List<T> source, int numberToAdd) {
        for (int x = 0; (x < numberToAdd) && (x < source.size()); x++) {
            dest.add(source.get(x));
        }
    }

    /**
     * @param agent GSA to find processing instances in
     * @return list of all processing instances in agent
     */
    private List<ProcessingUnitInstance> findInstances(GridServiceAgent agent) {
        List<ProcessingUnitInstance> processingInstances = new ArrayList<>();

        stream(agent.getGridServiceContainers().getContainers()).forEach(c -> addAll(processingInstances, c.getProcessingUnitInstances(configuration.getName())));

        return processingInstances;
    }

    /**
     * @param agent GSA to find empty GSCs in
     * @return list of empty GSCs in agent
     */
    private List<GridServiceContainer> findEmptyContainers(GridServiceAgent agent) {
        return stream(agent.getGridServiceContainers().getContainers()).filter(c -> c.getProcessingUnitInstances().length == 0).collect(toList());
    }

    /**
     * Relocates excess processing instances to empty GSCs
     *
     * @param availableContainers empty GSCs which excess instances will be relocated to
     * @param excessInstances excess instances to be relocated
     * @throws Exception if number of availableContainers < number of excessInstances
     */
    private void relocateExcessInstances(Set<GridServiceContainer> availableContainers, Set<ProcessingUnitInstance> excessInstances) throws Exception {
        if (isEmpty(availableContainers) || isEmpty(excessInstances)) {
            logger.info("No containers and/or no processing units to relocate.");
            return;
        }

        logger.info("Starting relocations...");
        Iterator<ProcessingUnitInstance> instanceIterator = excessInstances.iterator();
        Iterator<GridServiceContainer> containerIterator = availableContainers.iterator();

        while (instanceIterator.hasNext()) {
            ProcessingUnitInstance processingUnit = instanceIterator.next();

            if (containerIterator.hasNext()) {
                GridServiceContainer targetContainer = containerIterator.next();
                logger.info(String.format("Relocating processing unit [%s] to container [%s].", processingUnit.getUid(), targetContainer.getUid()));

                processingUnit.relocateAndWait(targetContainer, configuration.getTimeout(), TimeUnit.MILLISECONDS);
            } else {
                throw new Exception("No containers are available to finish relocations.");
            }
        }
    }
}
