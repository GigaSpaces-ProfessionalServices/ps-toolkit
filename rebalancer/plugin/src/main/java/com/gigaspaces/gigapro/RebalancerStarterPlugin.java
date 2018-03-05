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

import static com.gigaspaces.gigapro.InstanceUtil.findRebalancerInstance;

/**
 * @author Denys_Novikov
 * Date: 22.02.2018
 */
@CustomManagerResource
@Path("/controller")
public class RebalancerStarterPlugin {

    private static Logger logger = LoggerFactory.getLogger(RebalancerStarterPlugin.class);

    @Context
    Admin admin;

    @GET
    @Path("/start")
    public Response start(@QueryParam("appName") String appName) {
        try {
            ProcessingUnitInstance instance = findRebalancerInstance(admin, appName);
            if (instance == null) {
                return Response.status(404).entity("Rebalancer not found").build();
            }

            logger.info("Inside if ");
            instance.getSpaceInstance().getGigaSpace().write(new RebalancerStartEvent());
            logger.info("Object START saved to space");
            return Response.ok().entity("Rebalancer started").build();

        } catch (Throwable e) {
            logger.error("Error while starting rebalancer", e);
            return Response.status(500).entity("Error while starting rebalancer: " + e.getMessage()).build();
        }
    }

}
