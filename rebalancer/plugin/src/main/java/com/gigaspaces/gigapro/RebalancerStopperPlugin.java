package com.gigaspaces.gigapro;

import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.rest.CustomManagerResource;
import org.openspaces.admin.rest.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 * @author Denys_Novikov
 * Date: 22.02.2018
 */
@CustomManagerResource
@Path("/controller")
public class RebalancerStopperPlugin {

    private static Logger logger = LoggerFactory.getLogger(RebalancerStopperPlugin.class);

    @Context
    Admin admin;

    @GET
    @Path("/stop")
    public Response stop(@QueryParam("appName") String appName) {
        try {
            ProcessingUnitInstance instance = InstanceUtil.findRebalancerInstance(admin, appName);
            if (instance == null) {
                return Response.status(404).entity("Rebalancer not found").build();
            }

            logger.info("Inside if ");
            instance.getSpaceInstance().getGigaSpace().write(new RebalancerStopEvent());
            logger.info("Object STOP saved to space");
            return Response.ok().entity("Rebalancer stopped").build();

        } catch (Throwable e) {
            logger.error("Error while stopping rebalancer", e);
            return Response.status(500).entity("Error while stopping rebalancer: " + e.getMessage()).build();
        }
    }

}
