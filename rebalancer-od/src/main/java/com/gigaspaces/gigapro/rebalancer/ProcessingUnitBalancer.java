package com.gigaspaces.gigapro.rebalancer;

import com.gigaspaces.gigapro.rebalancer.config.Configuration;
import com.gigaspaces.gigapro.rebalancer.strategies.BalancerStrategy;
import com.gigaspaces.gigapro.rebalancer.strategies.ProcessorWithBackup;
import com.gigaspaces.gigapro.rebalancer.strategies.ProcessorWithoutBackup;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.zone.config.RequiredZonesConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by Skyler on 3/17/2016.
 */
public class ProcessingUnitBalancer {
    private Admin admin;
    private Configuration configuration;
    private Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    public ProcessingUnitBalancer(Admin admin, Configuration configuration){
        this.admin = admin;
        this.configuration = configuration;
    }

    public void balance() throws Exception {
        logger.info("Searching for agents...");
        GridServiceAgent[] agents = waitForAgents();

        logger.info(String.format("Searching for processing unit '%s'...", configuration.getName()));
        ProcessingUnit targetPu = waitForProcessingUnit();

        BalancerStrategy strategy = targetPu.getNumberOfBackups() != 0 ? new ProcessorWithBackup() : new ProcessorWithoutBackup(configuration);
        strategy.balance(targetPu, reduceAgentsByZone(targetPu, agents));
    }

    private List<GridServiceAgent> reduceAgentsByZone(ProcessingUnit processingUnits, GridServiceAgent[] agents) throws Exception {
        List<GridServiceAgent> output = new ArrayList<>();

        RequiredZonesConfig requiredContainerZones = processingUnits.getRequiredContainerZones();
        Set<String> processingUnitZones = requiredContainerZones.getZones();

        for(int x = 0; x < agents.length; x++) {
            GridServiceAgent agent = agents[x];
            Set<String> zones = agent.getExactZones().getZones();

            if(zones.size() == 0 && processingUnitZones.size() == 0){
                output.add(agent);
                continue;
            }

            for(String zone : processingUnitZones){
                if(zones.contains(zone)){
                    output.add(agent);
                    break;
                }
            }
        }

        if(output.size() == 0){
            throw new Exception("No agents were found matching the processing unit zone requirements.");
        }

        return output;
    }

    private GridServiceAgent[] waitForAgents() throws Exception {
        GridServiceAgents gridServiceAgents = admin.getGridServiceAgents();
        gridServiceAgents.waitFor(configuration.getMachines(), configuration.getTimeout(), TimeUnit.MILLISECONDS);
        GridServiceAgent[] agents = gridServiceAgents.getAgents();
        GridServiceAgent[] output = new GridServiceAgent[agents.length];

        if(output != null && output.length > 0){
            logger.info(String.format("Found %s running agent(s).", agents.length));
            for(int x =0; x < agents.length; x++) {
                GridServiceAgent agent = agents[x];
                logger.info(String.format("Agent [%s]", agent.getVirtualMachine().getDetails().getPid()));
                output[x] = agent;
            }
        }

        return output;
    }

    private ProcessingUnit waitForProcessingUnit() throws Exception {
        ProcessingUnits processingUnits = admin.getProcessingUnits();
        ProcessingUnit output = processingUnits.waitFor(configuration.getName(), configuration.getTimeout(), TimeUnit.MILLISECONDS);

        if(output == null){
            throw new Exception("Failed to find processing unit.");
        }

        return output;
    }
}
