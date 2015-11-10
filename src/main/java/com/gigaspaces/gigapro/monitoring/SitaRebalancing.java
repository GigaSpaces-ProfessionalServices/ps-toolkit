package com.gigaspaces.gigapro.monitoring;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.apache.commons.collections.MapUtils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SitaRebalancing implements InitializingBean {

    private String puName;

    private String zone;

    private Admin admin;

    public static void main(String[] args) {
        Admin admin = new AdminFactory().addGroup("pavlo").createAdmin();
        GridServiceAgents gridServiceAgents = admin.getGridServiceAgents();
        gridServiceAgents.getGridServiceAgentAdded()
        gridServiceAgents.waitFor(1);
        ProcessingUnits processingUnits = admin.getProcessingUnits();
        processingUnits.waitFor("spacee");
        for (GridServiceAgent agent : gridServiceAgents){
            ExactZonesConfig exactZones = agent.getExactZones();
            System.out.println("GSA zones = " + exactZones.getZones());
            GridServiceContainer[] containers = agent.getGridServiceContainers().getContainers();
            for (GridServiceContainer gsc : containers){
                ProcessingUnitInstance[] processingUnitInstances = gsc.getProcessingUnitInstances();
                ProcessingUnitInstance[] puis = gsc.getProcessingUnitInstances("spacee");
                for (ProcessingUnitInstance pu: puis){
                    System.out.println("PU zone " + pu.getZones().size());
                }
            }
        }

        for (ProcessingUnit processingUnit : processingUnits){
            System.out.println("PU zones = " + processingUnit.getRequiredContainerZones().getZones());
            processingUnit.getPlannedNumberOfInstances();

        }
    }


    private void rebalance(){
        // get GSAa with zone

        List<GridServiceAgent> gsas = getGridServiceAgents(zone);

        //get PU
        ProcessingUnit processingUnit = getProcessingUnit(puName);

        //get empty containers
        Map<GridServiceAgent, List<GridServiceContainer>> emptyContainersMap = buildEmptyContainersMap(admin.getGridServiceContainers());

        // check PU is deployed
        checkProcessingUnitDeployment(processingUnit);

        //how many unbalanced nodes can be?
        int instancesPerAgent = processingUnit.getInstances().length / gsas.size();
        int unbalancedNodes = processingUnit.getInstances().length % gsas.size();

        //check primaries
        Map<GridServiceAgent, Integer> lowPrimaries = new HashMap<>();
        Map<GridServiceAgent, Integer> highPrimaries = new HashMap<>();
        boolean unbalanced = false;
        for (GridServiceAgent gsa : gsas){
            int primaries = listPrimariesOnGSA(gsa).size();
            if (primaries > instancesPerAgent){
                highPrimaries.put(gsa, primaries);
            }
            if (primaries > instancesPerAgent + 1){
                unbalanced = true;
            }
            if (primaries < instancesPerAgent){
                lowPrimaries.put(gsa, primaries);
            }
        }

        if (!unbalanced && highPrimaries.size() == unbalancedNodes){
            System.out.println("BALANCED");
            System.exit(0);
        }

        boolean moved = false;
        for (GridServiceAgent gsa : highPrimaries.keySet()){
            List<ProcessingUnitInstance> processingUnitInstances = listPrimariesOnGSA(gsa);
            for (ProcessingUnitInstance pui : processingUnitInstances){
//                Look for a quick swap -- if backup on low primary machine, restart primary
                for (GridServiceAgent lowPrimaryGSA : lowPrimaries.keySet()){
                    if (backupIsOnLowPrimaryGSA(pui, lowPrimaryGSA)){
                        System.out.println("RESTARTING"); //TODO
                        pui.restartAndWait();
                        moved = true;
                        break;
                    }   else {
                        System.out.println("BACKUP NOT FOUND"); //TODO
                    }
                }
                if (moved) break;
            }
            if (moved) break;
        }

        for (GridServiceAgent gsa : highPrimaries.keySet()){
            List<ProcessingUnitInstance> processingUnitInstances = listPrimariesOnGSA(gsa);
            for (ProcessingUnitInstance pui : processingUnitInstances){
                Map<GridServiceAgent, GridServiceContainer> emptyContainersOnLowPrimaries = findEmptyContainersOnLowPrimaries(emptyContainersMap, pui, instancesPerAgent);
                if (MapUtils.isEmpty(emptyContainersMap)){
                    System.out.println();
                    continue;
                }

                for (GridServiceAgent targetGSA : emptyContainersOnLowPrimaries.keySet()){
                    String currentBackupGSA = pui.getSpaceInstance().getPartition().getBackup().getVirtualMachine().getGridServiceAgent().getUid();
                    System.out.println(String.format("moving backup id=%d from %s to %s",
                            pui.getInstanceId(), currentBackupGSA, targetGSA.getUid()));
                    findBackupInstanceForPrimary(pui).relocateAndWait(emptyContainersOnLowPrimaries.get(targetGSA));
                    System.out.println(String.format("backup on %s restarting primary", targetGSA.getUid()));
                    pui.restartAndWait();
                    moved = true;
                    break;
                }
                if (moved) break;
            }
            if (moved) break;
        }

        if (!moved){
            System.out.println("REBALANCE FAILED");
        }
    }

    private boolean backupIsOnLowPrimaryGSA(ProcessingUnitInstance pui, GridServiceAgent lowPrimaryGSA) {
        return lowPrimaryGSA.getMachine().getHostName().equals(pui.getSpaceInstance().getPartition().getBackup().getMachine().getHostName());
    }

    private List<ProcessingUnitInstance> listPrimariesOnGSA(GridServiceAgent gsa) {
        List<ProcessingUnitInstance> primaries = new ArrayList<>();
        for (GridServiceContainer gsc : gsa.getGridServiceContainers().getContainers()){
            for (ProcessingUnitInstance pui : gsc.getProcessingUnitInstances(puName)){
                if (pui.getSpaceInstance().getMode() == SpaceMode.PRIMARY){
                    primaries.add(pui);
                }
            }
        }
        return primaries;
    }

    private void checkProcessingUnitDeployment(ProcessingUnit processingUnit) {
        if (!processingUnit.getRequiredContainerZones().getZones().contains(zone)){
            //TODO what to do?
            System.out.println("PU doesn't need specified zone");
        }
        ProcessingUnitInstance[] instances = processingUnit.getInstances();
        if (instances.length < processingUnit.getPlannedNumberOfInstances()){
            //TODO what to do?
            System.out.println("PU is not deployed");
        }
    }

    private Map<GridServiceAgent, List<GridServiceContainer>> buildEmptyContainersMap(GridServiceContainers containers){
        Map<GridServiceAgent, List<GridServiceContainer>> result = new HashMap<>();
        for (GridServiceContainer gsc : containers){
            if (gsc.getProcessingUnitInstances().length == 0){
                GridServiceAgent gridServiceAgent = gsc.getGridServiceAgent();
                List<GridServiceContainer> emptyContainers = result.get(gridServiceAgent);
                if (emptyContainers == null){
                    emptyContainers = new ArrayList<>();
                    result.put(gridServiceAgent, emptyContainers);
                }
                emptyContainers.add(gsc);
            }
        }
        return result;
    }

    private Map<GridServiceAgent, GridServiceContainer> findEmptyContainersOnLowPrimaries(
            Map<GridServiceAgent, List<GridServiceContainer>> emptyContainers, ProcessingUnitInstance primary, int instancesPerAgent){
        Map<GridServiceAgent, GridServiceContainer> result = new HashMap<>();
        for (Map.Entry<GridServiceAgent, List<GridServiceContainer>> entry : emptyContainers.entrySet()){
            GridServiceContainer container = entry.getValue().get(0);
            if (isTodoname(container, primary, instancesPerAgent)){
                result.put(entry.getKey(), container);
                System.out.println(String.format("added %s as primary target", entry.getValue().get(0).getUid()));
//                    println "added ${entry.value[0].machine.hostName} as primary target"
//                    println "  inst_per=${inst_per}"
//                    println "  prime hostname=${primeinstance.machine.hostName}"
//                    println "  candidate hostname="${entry.value[0].machine.hostName}"
//                    println "  ignoreBackup=${ignoreBackup}"
//                    println "  prime backup hostname=${primeinstance.spaceInstance.partition.backup.machine.hostName}"
            }
        }
        return result;
    }


    private ProcessingUnitInstance findBackupInstanceForPrimary(ProcessingUnitInstance primary){
        return primary.getPartition().getBackup();
        //TODO check why the following code was used
//    def findBackupInstanceForPrimary(primaryinstance){
//
//        def backupmach=primaryinstance.spaceInstance.partition.backup.machine
//        for(inst in backupmach.getProcessingUnitInstances(primaryinstance.name)){
//            if(primaryinstance.spaceInstance.partition.backup.uid.equals(inst.spaceInstance.uid)){
//                return inst
//            }
//        }
//
//        println "no backup found for primary"
//        return null
//
//    }
    }



    private boolean isTodoname(GridServiceContainer gsc, ProcessingUnitInstance primary, int instancesPerAgent){
        return lowPrimaryAgent(gsc, instancesPerAgent) &&
                notTheSameAgentAsPrimary(gsc, primary) &&
                notTheSameAgentAsBackup(gsc, primary);
      }

    private boolean notTheSameAgentAsBackup(GridServiceContainer gsc, ProcessingUnitInstance primary) {
        return !primary.getSpaceInstance().getPartition().getBackup().getVirtualMachine().getGridServiceAgent().getUid().equals(gsc.getGridServiceAgent().getUid());
    }

    private boolean notTheSameAgentAsPrimary(GridServiceContainer gsc, ProcessingUnitInstance primary) {
        return !gsc.getGridServiceAgent().getUid().equals(primary.getGridServiceContainer().getGridServiceAgent().getUid());
    }

    private boolean lowPrimaryAgent(GridServiceContainer gsc, int instancesPerAgent) {
        return listPrimariesOnGSA(gsc.getGridServiceAgent()).size() < instancesPerAgent;
    }

    private List<GridServiceAgent> getGridServiceAgents(String zone) {
        List<GridServiceAgent> result = new ArrayList<>();
        //how to wait?
        GridServiceAgents gridServiceAgents = admin.getGridServiceAgents();
        gridServiceAgents.waitFor(1);
        for (GridServiceAgent gsa : gridServiceAgents){
            if (gsa.getExactZones().getZones().contains(zone)){
                result.add(gsa);
            }
        }
        return result;
    }

    private ProcessingUnit getProcessingUnit(String puName) {
        return admin.getProcessingUnits().getProcessingUnit(puName);
    }

    @Override
    public void afterPropertiesSet() throws Exception {


    }

}
