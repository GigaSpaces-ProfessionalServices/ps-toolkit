package com.gigaspaces.gigapro.rebalancing;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceAgents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Denys_Novikov
 * Date: 29.03.2018
 */
public class ZoneUtils {

    // filter GSAs by PU required zone
    public static List<GridServiceAgent> sortGridServiceAgentsByZones(Admin admin, Set<String> zones) {
        List<GridServiceAgent> result = new ArrayList<>();
        GridServiceAgents gridServiceAgents = admin.getGridServiceAgents();
        gridServiceAgents.waitFor(1);

        // if pu zones is empty, any agent can service it
        if (zones == null || zones.isEmpty()) {
            return Arrays.asList(gridServiceAgents.getAgents());
        }

        for (GridServiceAgent gsa : gridServiceAgents){
            Set<String> gsaZones = gsa.getExactZones().getZones();
            for (String zone : zones){
                if (gsaZones.contains(zone)){
                    result.add(gsa);
                    break;
                }
            }
        }
        return result;
    }
}
