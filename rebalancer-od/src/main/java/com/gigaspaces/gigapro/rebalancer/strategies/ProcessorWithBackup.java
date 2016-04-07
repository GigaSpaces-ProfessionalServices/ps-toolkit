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

import static com.gigaspaces.gigapro.rebalancer.strategies.ProcessorCommons.*;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Math.*;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.frequency;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@SuppressWarnings("Duplicates")
public class ProcessorWithBackup implements BalancerStrategy {
    private Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private Configuration configuration;

    private int instancesNum;
    private int backupsNum;
    private int primariesNum;
    private int ceilPerMachine;
    private int floorPerMachine;
    private int primariesPerMachine;
    private int machinesWithExtraPrimary;

    public ProcessorWithBackup(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void balance(ProcessingUnit targetPu, List<GridServiceAgent> gridServiceAgents) throws Exception {
        logger.info("Using the backup-full rebalancing strategy.");

        initParameters(targetPu, gridServiceAgents);

        if (gridServiceAgents.size() == 1)
            throw new Exception("Deployment would be compromised as only one machine is available to host all primaries and their backups.");
        if (findAllContainers(gridServiceAgents).size() != instancesNum)
            throw new Exception("There's not enough containers to store all processing instances.");
        if (primariesNum != backupsNum)
            throw new Exception("The number of primaries is not equal to the number of backups.");

        logger.info(format("Each GSA will host %s instance(s).", floorPerMachine));
        if (machinesWithExtraPrimary > 0)
            logger.info(format("%s GSAs will host %s instance(s).", machinesWithExtraPrimary, ceilPerMachine));

        logger.info("Starting shuffling odd instances...");
        shuffle(gridServiceAgents);
        logger.info("Starting normalization...");
        normalize(gridServiceAgents);
    }

    /**
     * This is the first step of rebalancing process.
     * On this step all odd instances from all GSAs are moved to another GSAs.
     * As a result, each GSA contains even number of primaries and backups and
     * no primary shares the same GSA as its backup instance.
     *
     * @param gridServiceAgents list of GSAs
     */
    private void shuffle(List<GridServiceAgent> gridServiceAgents) {
        while (!validate(gridServiceAgents)) {
            Set<ProcessingUnitInstance> oddInstances = findOdd(gridServiceAgents);
            Iterator<ProcessingUnitInstance> instanceIterator = oddInstances.iterator();
            while (instanceIterator.hasNext()) {
                ProcessingUnitInstance instance = instanceIterator.next();
                if (findAllEmptyContainers(gridServiceAgents).isEmpty() && validateForUniqueInstances(gridServiceAgents)) {
                    instance.restartAndWait(configuration.getTimeout(), TimeUnit.MILLISECONDS);
                }
                List<GridServiceContainer> containersToRelocate = getContainersToRelocate(gridServiceAgents, instance);
                for (GridServiceContainer container : containersToRelocate) {
                    if (tryRelocate(instance, container)) {
                        instanceIterator.remove();
                        break;
                    }
                }
            }
        }
    }

    /**
     * This is the second step of rebalancing process.
     * Here all instances within each GSA are evenly distributed (1 instance per container).
     *
     * @param gridServiceAgents list of GSAs to normalize
     */
    private void normalize(List<GridServiceAgent> gridServiceAgents) {
        while (isNotEmpty(findAllEmptyContainers(gridServiceAgents))) {
            gridServiceAgents.forEach(this::normalizeAgent);
        }
    }

    /**
     *  This method evenly (1 instance per container) distributes all instances on containers in the GSA.
     *
     * @param agent GSA to normalize
     */
    private void normalizeAgent(GridServiceAgent agent) {
        Set<GridServiceContainer> availableContainers = newHashSet(findEmptyContainers(agent));
        if (isNotEmpty(availableContainers)) {
            try {
                relocateExcessInstances(availableContainers, getExcessInstances(agent), configuration);
            } catch (Exception e){/*NOP*/}
        }
    }

    /**
     * @param gridServiceAgents GSAs to find in
     * @return Set of odd items in the GSAs
     */
    private Set<ProcessingUnitInstance> findOdd(List<GridServiceAgent> gridServiceAgents) {
        return gridServiceAgents.stream().flatMap(a -> findOdd(a).stream()).collect(toSet());
    }

    /**
     * @param agent GSA to find in
     * @return Set of odd items in the GSA
     */
    private Set<ProcessingUnitInstance> findOdd(GridServiceAgent agent) {
        if (!validateAgent(agent)) {
            List<ProcessingUnitInstance> instances = getInstances(agent);

            int instancesCount = instances.size();
            int primariesCount = (int) instances.stream().filter(ProcessorCommons::isPrimary).count();
            int backupsCount = (int) instances.stream().filter(ProcessorCommons::isBackup).count();

            // Here we check if there're not unique (by name) instances in the GSA
            List<String> instancesNames = instances.stream().map(ProcessorCommons::getInstanceName).collect(toList());
            Set<String> notUniqueInstancesNames = instancesNames.stream().filter(n -> frequency(instancesNames, n) > 1).collect(toSet());

            // If found not unique instances
            if (isNotEmpty(notUniqueInstancesNames)) {
                Set<ProcessingUnitInstance> notUniqueInstances = instances.stream().filter(i -> notUniqueInstancesNames.contains(getInstanceName(i))).collect(toSet());
                Set<ProcessingUnitInstance> oddBackups = notUniqueInstances.stream().filter(ProcessorCommons::isBackup).collect(toSet());
                Set<ProcessingUnitInstance> oddPrimaries = notUniqueInstances.stream().filter(ProcessorCommons::isPrimary).collect(toSet());
                if (primariesCount < primariesPerMachine) {
                    int toMove = floorPerMachine - primariesCount - primariesPerMachine;
                    toMove = toMove == 0 ? 1 : toMove;
                    return oddBackups.stream().limit(toMove).collect(toSet());
                } else if (primariesCount > primariesPerMachine) {
                    int toMove = primariesCount - primariesPerMachine;
                    toMove = toMove == 0 ? 1 : toMove;
                    return oddPrimaries.stream().limit(toMove).collect(toSet());
                } else {
                    int toMove = abs(instancesCount - ceilPerMachine);
                    toMove = toMove == 0 ? 1 : toMove;
                    return oddBackups.stream().limit(toMove).collect(toSet());
                }
            }

            // If all instances in the GSA are unique (by name)
            if (primariesCount < primariesPerMachine) {
                int toMove = floorPerMachine - primariesCount - primariesPerMachine;
                toMove = toMove == 0 ? 1 : toMove;
                return instances.stream().filter(ProcessorCommons::isBackup).limit(toMove).collect(toSet());
            } else if (primariesCount > primariesPerMachine) {
                int toMove = primariesCount - primariesPerMachine;
                toMove = toMove == 0 ? 1 : toMove;
                return instances.stream().filter(ProcessorCommons::isPrimary).limit(toMove).collect(toSet());
            } else {
                int toMove = abs(instancesCount - ceilPerMachine);
                toMove = toMove == 0 ? 1 : toMove;

                // if only 1 instance needs to be moved we choose it randomly to prevent endless loop in shuffle()
                // which is possible if this instance doesn't fit to any GSAs
                if (toMove <= 1) {
                    Set<ProcessingUnitInstance> result = new HashSet<>();
                    ProcessingUnitInstance randomBackup = instances.stream().filter(ProcessorCommons::isBackup).collect(toList()).get(new Random().nextInt(backupsCount));
                    result.add(randomBackup);
                    return result;
                }
                return instances.stream().filter(ProcessorCommons::isBackup).limit(toMove).collect(toSet());
            }
        }
        return Collections.emptySet();
    }

    /**
     * @param instance instance to relocate
     * @param newContainer container which the instance must be relocated to
     * @return true - if instance relocated successfully, <br> false - otherwise
     */
    private boolean tryRelocate(ProcessingUnitInstance instance, GridServiceContainer newContainer) {
        GridServiceAgent newAgent = newContainer.getGridServiceAgent();
        GridServiceAgent oldAgent = instance.getGridServiceContainer().getGridServiceAgent();
        if (!newAgent.equals(oldAgent)) {
            if (getInstancesByName(newAgent, getInstanceName(instance)).isEmpty()) {
                logger.info(format("Relocating processing unit [%s] to container [%s].", instance.getUid(), newContainer.getUid()));
                instance.relocateAndWait(newContainer, configuration.getTimeout(), TimeUnit.MILLISECONDS);
                return true;
            }
        }
        return false;
    }

    /**
     * @param gridServiceAgents GSAs to validate
     * @return true - if instances on GSAs are balanced well, <br> false - otherwise
     */
    private boolean validate(List<GridServiceAgent> gridServiceAgents) {
        return !gridServiceAgents.stream().map(this::validateAgent).filter(i -> !i).findFirst().isPresent();
    }

    /**
     * Validates if GSA is balanced well.
     *
     * @param agent GSA to validate
     * @return true - if instances on the GSA are balanced well, <br> false - otherwise
     */
    private boolean validateAgent(GridServiceAgent agent) {
        List<ProcessingUnitInstance> instances = getInstances(agent);
        if (instances.size() > ceilPerMachine) return false;

        long primaries = instances.stream().filter(ProcessorCommons::isPrimary).count();
        long backups = instances.stream().filter(ProcessorCommons::isBackup).count();
        if (primaries > primariesPerMachine || abs(primaries - backups) > 1) return false;

        return validateAgentForUniqueInstances(agent);
    }

    /**
     * @param gridServiceAgents GSAs to validate
     * @return true - if instances on all GSAs are unique by name, <br> false - otherwise
     */
    private boolean validateForUniqueInstances(List<GridServiceAgent> gridServiceAgents) {
        return !gridServiceAgents.stream().map(this::validateAgentForUniqueInstances).filter(i -> !i).findFirst().isPresent();
    }

    /**
     * @param agent GSA to validate
     * @return true - if instances on the GSA are unique by name, <br> false - otherwise
     */
    private boolean validateAgentForUniqueInstances(GridServiceAgent agent) {
        List<ProcessingUnitInstance> instances = getInstances(agent);
        long distinctInstances = instances.stream().map(ProcessorCommons::getInstanceName).distinct().count();
        return distinctInstances == instances.size();
    }

    /**
     * @param agent GSA to find processing instances in
     * @return list of all processing instances in GSA
     */
    private List<ProcessingUnitInstance> getInstances(GridServiceAgent agent) {return findInstances(agent, configuration.getName());}

    /**
     * @param agent GSA to find processing instances in
     * @param name name of instance
     * @return Set of instances with given name (both primaries and backups)
     */
    private Set<ProcessingUnitInstance> getInstancesByName(GridServiceAgent agent, String name) {
        return getInstances(agent).stream().filter(i -> getInstanceName(i).equals(name)).collect(toSet());
    }

    /**
     * @param gridServiceAgents GSAs to scan for GSCs
     * @param instance instance to relocate
     * @return if there're empty GSCs they are returned, otherwise GSCs of not instance's GSAs are returned
     */
    private List<GridServiceContainer> getContainersToRelocate(List<GridServiceAgent> gridServiceAgents, ProcessingUnitInstance instance) {
        List<GridServiceContainer> emptyContainers = findAllEmptyContainers(gridServiceAgents);
        if (emptyContainers.isEmpty()) {
            List<GridServiceAgent> otherAgents = gridServiceAgents.stream().filter(a -> !a.equals(instance.getGridServiceContainer().getGridServiceAgent())).collect(toList());
            emptyContainers = findAllContainers(otherAgents);
        }
        return emptyContainers;
    }

    /**
     * @param agent GSA to find excess processing instances in
     * @return Set of excess instances from every container in the GSA containing more than 1 instance
     */
    private Set<ProcessingUnitInstance> getExcessInstances(GridServiceAgent agent) {
        return stream(agent.getGridServiceContainers().getContainers())
                .filter(c -> c.getProcessingUnitInstances().length > 1)
                .flatMap(c -> stream(c.getProcessingUnitInstances(configuration.getName())).limit(1))
                .collect(toSet());
    }

    private void initParameters(ProcessingUnit targetPu, List<GridServiceAgent> gridServiceAgents) {
        int machinesNum = gridServiceAgents.size();
        instancesNum = targetPu.getPlannedNumberOfInstances();
        primariesNum = (int) gridServiceAgents.stream().flatMap(a -> getInstances(a).stream()).filter(ProcessorCommons::isPrimary).count();
        backupsNum = (int) gridServiceAgents.stream().flatMap(a -> getInstances(a).stream()).filter(ProcessorCommons::isBackup).count();
        ceilPerMachine = (int) ceil((double) instancesNum / machinesNum);
        floorPerMachine = (int) floor((double) instancesNum / machinesNum);
        primariesPerMachine = (int) ceil((double) primariesNum / machinesNum);
        machinesWithExtraPrimary = ceilPerMachine - floorPerMachine == 0 ? 0 : primariesNum - machinesNum * (primariesPerMachine - 1);
    }
}

