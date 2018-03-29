package com.gigaspaces.gigapro;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.util.concurrent.TimeUnit;

/**
 * @author Denys_Novikov
 * Date: 01.03.2018
 */
public class InstanceUtil {

    public static ProcessingUnitInstance findRebalancerInstance(Admin admin, String appName) {
        admin.getGridServiceAgents().waitForAtLeastOne(10, TimeUnit.SECONDS);
        for (GridServiceAgent agent : admin.getGridServiceAgents().getAgents()) {
            for (GridServiceContainer container : agent.getMachine().getGridServiceContainers().getContainers()) {
                for (ProcessingUnitInstance instance : container.getProcessingUnitInstances()) {
                    if (appName.equals(instance.getProcessingUnit().getName())) {
                        return instance;
                    }
                }
            }
        }
        return null;
    }
}
