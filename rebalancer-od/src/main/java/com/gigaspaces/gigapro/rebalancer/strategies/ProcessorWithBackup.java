package com.gigaspaces.gigapro.rebalancer.strategies;

import com.gigaspaces.gigapro.rebalancer.Constants;
import com.gigaspaces.gigapro.rebalancer.config.Configuration;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.gigaspaces.gigapro.rebalancer.strategies.ProcessorCommons.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Integer.compare;
import static java.lang.Math.*;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.frequency;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.addAll;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

public class ProcessorWithBackup implements BalancerStrategy {
    private final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final Configuration configuration;
    private final Admin admin;

    private int instancesNum;
    private int backupsNum;
    private int primariesNum;
    private int ceilPerMachine;
    private int floorPerMachine;
    private int primariesPerMachine;
    private int machinesWithExtraInstance;
    List<GridServiceAgent> gridServiceAgents;

    public ProcessorWithBackup(Admin admin, Configuration configuration) {
        this.admin = admin;
        this.configuration = configuration;
    }

    @Override
    public void balance(ProcessingUnit targetPu, List<GridServiceAgent> GSAs) throws Exception {
        logger.info("Using the backup-full rebalancing strategy.");

        initParameters(targetPu, GSAs);

        if (gridServiceAgents.size() == 1)
            throw new Exception("Deployment would be compromised as only one machine is available to host all primaries and their backups.");
        if (findAllContainers(gridServiceAgents).size() < instancesNum)
            throw new Exception("There's not enough containers to store all processing instances.");
        if (primariesNum != backupsNum)
            throw new Exception("The number of primaries is not equal to the number of backups.");


        if (machinesWithExtraInstance > 0) {
            int machinesWithoutExtraInstance = GSAs.size() - machinesWithExtraInstance;
            logger.info(format("%s GSA(s) will host %s instance(s) & %s GSA(s) will host %s instance(s).",
                    machinesWithExtraInstance, ceilPerMachine, machinesWithoutExtraInstance, floorPerMachine));
        } else
            logger.info(format("Each GSA will host %s instance(s).", floorPerMachine));

        logger.info("Starting shuffling odd instances...");
        shuffle();
        logger.info("Shuffling is finished.");
        logger.info("Starting normalization...");
        normalize();
        logger.info("Normalization is finished.");
    }

    /**
     * This is the first step of rebalancing process.
     * On this step all odd instances from all GSAs are moved to another GSAs.
     * As a result, each GSA contains even number of primaries and backups and
     * no primary shares the same GSA as its backup instance.
     */
    private void shuffle() {
        while (!validate()) {
            Set<ProcessingUnitInstance> oddInstances = findOdd();
            Iterator<ProcessingUnitInstance> instanceIterator = oddInstances.iterator();
            while (instanceIterator.hasNext()) {
                ProcessingUnitInstance instance = instanceIterator.next();
                List<GridServiceContainer> containersToRelocate = getContainersToRelocate(instance);
                containersToRelocate.stream().filter(container -> tryRelocate(instance, container)).findFirst().ifPresent(c -> {
                    instanceIterator.remove();
                    updateGridServiceAgents();
                });
            }
        }
    }

    /**
     * This is the second step of rebalancing process.
     * Here all instances within each GSA are evenly distributed (1 instance per container).
     */
    private void normalize() {
        int allowableEmptyContainersNum = findAllContainers(gridServiceAgents).size() - instancesNum;
        while (findAllEmptyContainers(gridServiceAgents).size() > allowableEmptyContainersNum) {
            gridServiceAgents.forEach(this::normalizeAgent);
        }
    }

    /**
     * This method evenly (1 instance per container) distributes all instances on containers in the GSA.
     *
     * @param agent GSA to normalize
     */
    private void normalizeAgent(GridServiceAgent agent) {
        Set<GridServiceContainer> availableContainers = newHashSet(findEmptyContainers(agent));
        if (isNotEmpty(availableContainers)) {
            try {
                relocateExcessInstances(availableContainers, getExcessInstances(agent), configuration);
                updateGridServiceAgents();
            } catch (Exception e) {/*NOP*/}
        }
    }

    /**
     * @return Set of odd items in the GSAs
     */
    private Set<ProcessingUnitInstance> findOdd() {
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

            Set<ProcessingUnitInstance> notUniqueInstances = getNotUniqueInstances(instances);
            if (isNotEmpty(notUniqueInstances)) {
                return getInstancesToMove(instancesCount, primariesCount, notUniqueInstances);
            }
            return getInstancesToMove(instancesCount, primariesCount, instances);
        }
        return Collections.emptySet();
    }

    /**
     * @param instances list of instances
     * @return Set of not unique instances (by name)
     */
    private Set<ProcessingUnitInstance> getNotUniqueInstances(List<ProcessingUnitInstance> instances) {
        List<String> instancesNames = instances.stream().map(ProcessorCommons::getInstanceName).collect(toList());
        Set<String> notUniqueInstancesNames = instancesNames.stream().filter(n -> frequency(instancesNames, n) > 1).collect(toSet());
        return instances.stream().filter(i -> notUniqueInstancesNames.contains(getInstanceName(i))).collect(toSet());
    }

    /**
     * @param instancesCount number of instances in the GSA (needed to count the number of instances to move)
     * @param primariesCount number of primaries in the GSA (needed to count the number of instances to move)
     * @param instances      Collection of instances
     * @return Set of either primaries or backups to be moved out from the GSA
     */
    private Set<ProcessingUnitInstance> getInstancesToMove(int instancesCount, int primariesCount, Collection<ProcessingUnitInstance> instances) {
        List<ProcessingUnitInstance> backups = instances.stream().filter(ProcessorCommons::isBackup).collect(toList());
        List<ProcessingUnitInstance> primaries = instances.stream().filter(ProcessorCommons::isPrimary).collect(toList());
        int toMove = calculateNumberOfInstancesToMove(instancesCount, primariesCount);
        if (primariesCount > primariesPerMachine) {
            return getRandomInstances(primaries, toMove);
        } else if (primariesCount < primariesPerMachine) {
            return getRandomInstances(backups, toMove);
        } else {
            return getRandomInstances(newArrayList(instances), toMove);
        }
    }

    /**
     * @param instancesCount number of instances in the GSA
     * @param primariesCount number of primaries in the GSA
     * @return Number of odd instances to be moved from the GSA
     */
    private int calculateNumberOfInstancesToMove(int instancesCount, int primariesCount) {
        int toMove;
        if (primariesCount < primariesPerMachine) {
            toMove = floorPerMachine - primariesCount - primariesPerMachine;
        } else if (primariesCount > primariesPerMachine) {
            toMove = primariesCount - primariesPerMachine;
        } else {
            toMove = abs(instancesCount - ceilPerMachine);
        }
        return toMove == 0 ? 1 : toMove;
    }

    /**
     * @param instances list of PU instances
     * @param limit     number of instances to return
     * @return Random subset of <i>instances</i> limited by <i>limit</i>.<br/>
     * <p>
     * If <i>instances.size()</i> < <i>limit</i>, then Set of <i>instances</i> is returned.
     */
    private Set<ProcessingUnitInstance> getRandomInstances(List<ProcessingUnitInstance> instances, int limit) {
        Set<ProcessingUnitInstance> result = new HashSet<>();
        if (limit > instances.size())
            return newHashSet(instances);

        Random random = new Random();
        while (result.size() < limit) {
            int index = random.nextInt(instances.size());
            ProcessingUnitInstance randomInstance = instances.get(index);
            result.add(randomInstance);
        }
        return result;
    }

    /**
     * @param instance     instance to relocate
     * @param newContainer container which the instance must be relocated to
     * @return true - if instance relocated successfully, <br> false - otherwise
     */
    private boolean tryRelocate(ProcessingUnitInstance instance, GridServiceContainer newContainer) {
        GridServiceAgent newAgent = newContainer.getGridServiceAgent();
        GridServiceAgent oldAgent = instance.getGridServiceContainer().getGridServiceAgent();
        if (!newAgent.equals(oldAgent) && getInstancesByName(newAgent, getInstanceName(instance)).isEmpty()) {
            logger.info(format("Relocating processing unit [%s] to container [%s].", instance.getProcessingUnitInstanceName(), newContainer.getVirtualMachine().getDetails().getPid()));
            instance.relocateAndWait(newContainer, configuration.getTimeout(), TimeUnit.MILLISECONDS);
            return true;
        }
        return false;
    }

    /**
     * @return true - if instances on GSAs are balanced well, <br> false - otherwise
     */
    private boolean validate() {
        // Uncomment for showing GSAs state in logs
        //gridServiceAgents.forEach(this::logAgentState);
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
        long primaries = instances.stream().filter(ProcessorCommons::isPrimary).count();
        long backups = instances.stream().filter(ProcessorCommons::isBackup).count();

        if (instances.size() > ceilPerMachine) return false;
        if (primaries > primariesPerMachine || abs(primaries - backups) > 1) return false;
        if (!validateAgentForUniqueInstances(agent)) return false;

        return isClusterEvenlyBalanced() || instances.size() <= floorPerMachine;

    }

    /**
     * @return true - if there's no any GSA with number of instances less then floorPerMachine, <br> false - otherwise
     */
    private boolean isClusterEvenlyBalanced() {
        return !gridServiceAgents.stream().filter(a -> getInstances(a).size() < floorPerMachine).findFirst().isPresent();
    }

    private void logAgentState(GridServiceAgent agent) {
        List<ProcessingUnitInstance> instances = getInstances(agent);
        long primaries = instances.stream().filter(ProcessorCommons::isPrimary).count();
        long backups = instances.stream().filter(ProcessorCommons::isBackup).count();
        long uniqueInstances = instances.stream().map(ProcessorCommons::getInstanceName).distinct().count();

        String instancesNames = instances.stream().map(i -> (isPrimary(i) ? "P:" : "B:") + i.getProcessingUnitInstanceName()).reduce("", (a, b) -> a + " " + b);
        logger.info(format("GSA [%s] stores %s processing units: %s", agent.getVirtualMachine().getDetails().getPid(), instances.size(), instancesNames));
        logger.info(format("Primaries: %s, Backups: %s", primaries, backups));
        logger.info(format("Unique instances: %s", uniqueInstances));
    }

    /**
     * @param agent GSA to validate
     * @return true - if instances on the GSA are unique by name, <br> false - otherwise
     */
    private boolean validateAgentForUniqueInstances(GridServiceAgent agent) {
        List<ProcessingUnitInstance> instances = getInstances(agent);
        long uniqueInstances = instances.stream().map(ProcessorCommons::getInstanceName).distinct().count();
        return uniqueInstances == instances.size();
    }

    /**
     * @param agent GSA to find processing instances in
     * @return list of all processing instances in GSA
     */
    private List<ProcessingUnitInstance> getInstances(GridServiceAgent agent) {return findInstances(agent, configuration.getName());}

    /**
     * @param agent GSA to find processing instances in
     * @param name  name of instance
     * @return Set of instances with given name (both primaries and backups)
     */
    private Set<ProcessingUnitInstance> getInstancesByName(GridServiceAgent agent, String name) {
        return getInstances(agent).stream().filter(i -> getInstanceName(i).equals(name)).collect(toSet());
    }

    /**
     * @param instance instance to relocate
     * @return if there're empty GSCs they are returned, otherwise GSCs of not instance's GSAs are returned
     */
    private List<GridServiceContainer> getContainersToRelocate(ProcessingUnitInstance instance) {
        List<GridServiceAgent> otherAgents = gridServiceAgents.stream()
                .filter(a -> !a.equals(instance.getGridServiceContainer().getGridServiceAgent()))
                .sorted((a1, a2) -> compare(findEmptyContainers(a2).size(), findEmptyContainers(a1).size()))
                .collect(toList());
        List<GridServiceContainer> containersToRelocate = findAllEmptyContainers(otherAgents);
        if (containersToRelocate.isEmpty()) {
            containersToRelocate = findAllContainers(otherAgents);
        }
        return containersToRelocate;
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

    private void initParameters(ProcessingUnit targetPu, List<GridServiceAgent> GSAs) {
        gridServiceAgents = GSAs;
        int machinesNum = gridServiceAgents.size();
        instancesNum = targetPu.getPlannedNumberOfInstances();
        primariesNum = (int) gridServiceAgents.stream().flatMap(a -> getInstances(a).stream()).filter(ProcessorCommons::isPrimary).count();
        backupsNum = (int) gridServiceAgents.stream().flatMap(a -> getInstances(a).stream()).filter(ProcessorCommons::isBackup).count();
        ceilPerMachine = (int) ceil((double) instancesNum / machinesNum);
        floorPerMachine = (int) floor((double) instancesNum / machinesNum);
        primariesPerMachine = (int) ceil((double) primariesNum / machinesNum);
        machinesWithExtraInstance = instancesNum % GSAs.size();
    }

    private void updateGridServiceAgents() {
        logger.info("Updating GSAs state...");
        GridServiceAgents freshAgents = admin.getGridServiceAgents();
        freshAgents.waitFor(configuration.getMachines(), configuration.getTimeout(), TimeUnit.MILLISECONDS);
        gridServiceAgents.clear();
        addAll(gridServiceAgents, freshAgents.getAgents());
    }
}

