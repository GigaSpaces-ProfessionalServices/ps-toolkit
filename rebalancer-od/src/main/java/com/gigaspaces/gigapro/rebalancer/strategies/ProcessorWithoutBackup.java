package com.gigaspaces.gigapro.rebalancer.strategies;

import com.gigaspaces.gigapro.rebalancer.Constants;
import com.gigaspaces.gigapro.rebalancer.config.Configuration;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ProcessorWithoutBackup implements BalancerStrategy {
    private Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private Configuration configuration;

    public ProcessorWithoutBackup(Configuration configuration){
        this.configuration = configuration;
    }

    @Override
    public void balance(ProcessingUnit targetPu, List<GridServiceAgent> gridServiceAgents) throws Exception {
        logger.info("Using the backup-less rebalancing strategy.");
        int totalMembers = targetPu.getPlannedNumberOfInstances();

        // The number of agents with an extra instance
        int machinesWithExtraInstance = totalMembers % gridServiceAgents.size();

        // Number of instances on each machine (not including imbalance instances)
        int evenInstanceCount = totalMembers / gridServiceAgents.size();

        // Corrects the upper bound number of processing unit on a machine.
        int agentCeil = evenInstanceCount + (machinesWithExtraInstance > 0 ? 1 : 0);

        logger.info(String.format("Each GSA will host %s instance(s).", evenInstanceCount));

        if(machinesWithExtraInstance > 0)
            logger.info(String.format("%s GSAs will host %s instance(s).", machinesWithExtraInstance, (evenInstanceCount + 1)));

        Set<GridServiceContainer> availableContainers = new HashSet<>();
        Set<ProcessingUnitInstance> excessInstances = new HashSet<>();

        populateAgentCollections(gridServiceAgents, agentCeil, availableContainers, excessInstances);

        balance(availableContainers, excessInstances);
    }

    private void balance(Set<GridServiceContainer> availableContainer, Set<ProcessingUnitInstance> excessInstances) throws Exception {

        if(availableContainer.size() == 0 || excessInstances.size() == 0){
            logger.info("No containers and/or no processing units to relocate.");
            return;
        }

        logger.info("Starting relocations...");
        Iterator<ProcessingUnitInstance> instanceIterator = excessInstances.iterator();
        Iterator<GridServiceContainer> containerIterator = availableContainer.iterator();

        while(instanceIterator.hasNext()){
            ProcessingUnitInstance processingUnit = instanceIterator.next();

            if(containerIterator.hasNext()){
                GridServiceContainer targetContainer = containerIterator.next();
                logger.info(String.format("Relocating processing unit [%s] to container [%s].", processingUnit.getUid(), targetContainer.getUid()));

                processingUnit.relocateAndWait(targetContainer, configuration.getTimeout(), TimeUnit.MILLISECONDS);
            } else {
                throw new Exception("No containers are available to finish relocations.");
            }
        }
    }

    private void populateAgentCollections(List<GridServiceAgent> gridServiceAgents, int agentCeil, Set<GridServiceContainer> availableContainers, Set<ProcessingUnitInstance> excessInstances) throws Exception {
        for(GridServiceAgent agent : gridServiceAgents){
            List<GridServiceContainer> emptyContainers = new ArrayList<>();
            List<ProcessingUnitInstance> agentInstances = findInstances(agent, emptyContainers);
            long pid = agent.getVirtualMachine().getDetails().getPid();
            int instanceCount = agentInstances.size();

            String operation;

            if(instanceCount > agentCeil) {
                operation = "DECREMENT COUNT";
                addExcessToList(excessInstances, agentInstances, instanceCount, agentCeil);
            }
            else if(instanceCount < agentCeil) {
                operation = "INCREMENT COUNT";
                addExcessToList(availableContainers, emptyContainers, instanceCount, agentCeil);
            } else {
                operation = "NO ACTION";
            }

            logger.info(String.format("[ Process: %s, Managed Instances: %s, Operation: %s ]", pid, instanceCount, operation));
        }
    }

    private <T> void addExcessToList(Set<T> output, List<T> input, int instanceCount, int agentCeil){
        int numberToAdd = Math.abs(agentCeil - instanceCount);

        for(int x = 0; (x < numberToAdd) && (x < input.size()); x++){
            output.add(input.get(x));
        }
    }

    private List<ProcessingUnitInstance> findInstances(GridServiceAgent agent, List<GridServiceContainer> emptyContainers) throws Exception {
        List<ProcessingUnitInstance> output = new ArrayList<>();

        GridServiceContainers gridServiceContainers = agent.getGridServiceContainers();


        for(GridServiceContainer container : gridServiceContainers.getContainers()){
            ProcessingUnitInstance[] containerInstances = container.getProcessingUnitInstances(configuration.getName());

            if(container.getProcessingUnitInstances().length == 0){
                emptyContainers.add(container);
            }

            for(ProcessingUnitInstance instance : containerInstances){
                output.add(instance);
            }
        }

        return output;
    }
}
