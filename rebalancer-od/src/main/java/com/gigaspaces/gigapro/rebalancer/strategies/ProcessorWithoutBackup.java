package com.gigaspaces.gigapro.rebalancer.strategies;

import com.gigaspaces.gigapro.rebalancer.Constants;
import com.gigaspaces.gigapro.rebalancer.config.Configuration;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static com.gigaspaces.gigapro.rebalancer.strategies.ProcessorCommons.*;

public class ProcessorWithoutBackup implements BalancerStrategy {
    private final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final Configuration configuration;

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

        relocateExcessInstances(availableContainers, excessInstances, configuration);
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
            List<ProcessingUnitInstance> agentInstances = findInstances(agent, configuration.getName());
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


}
