package com.gigaspaces.gigapro;

import org.openspaces.admin.Admin;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.rest.CustomManagerResource;
import org.openspaces.admin.rest.Response;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 * @author Denys_Novikov
 * Date: 22.02.2018
 */
@CustomManagerResource
@Path("/demo")
public class RebalancerPlugin {

    @Context
    Admin admin;

    @GET
    @Path("/report")
    public Response report(@QueryParam("hostname") String hostname) {
        Machine machine = admin.getMachines().getMachineByHostName(hostname);
        if (machine == null)
            return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity("Host not found").build();

        StringBuilder result = new StringBuilder("Custom report: host=" + hostname +
                ", containers=" + machine.getGridServiceContainers() +
                ", PU instances=");
        for (ProcessingUnitInstance pu : machine.getProcessingUnitInstances()) {
            result.append(pu.getProcessingUnitInstanceName()).append(",");
        }
        return Response.ok().entity(result.toString()).build();
    }
}
