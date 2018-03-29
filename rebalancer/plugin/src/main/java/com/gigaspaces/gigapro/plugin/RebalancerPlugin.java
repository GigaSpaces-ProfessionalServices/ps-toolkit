package com.gigaspaces.gigapro.plugin;

import com.gigaspaces.gigapro.*;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.rest.CustomManagerResource;
import org.openspaces.admin.rest.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
public class RebalancerPlugin {

    private static final int TIMEOUT = 10000; // 10 sec
    private static Logger logger = LoggerFactory.getLogger(RebalancerPlugin.class);

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

            instance.getSpaceInstance().getGigaSpace().write(new RebalancerStartEvent());
            logger.info("Object START saved to space");
            return Response.ok().entity("Rebalancer started").build();

        } catch (Throwable e) {
            logger.error("Error while starting rebalancer", e);
            return Response.status(500).entity("Error while starting rebalancer: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/stop")
    public Response stop(@QueryParam("appName") String appName) {
        try {
            ProcessingUnitInstance instance = findRebalancerInstance(admin, appName);
            if (instance == null) {
                return Response.status(404).entity("Rebalancer not found").build();
            }

            instance.getSpaceInstance().getGigaSpace().write(new RebalancerStopEvent());
            logger.info("Object STOP saved to space");
            return Response.ok().entity("Rebalancer stopped").build();

        } catch (Throwable e) {
            logger.error("Error while stopping rebalancer", e);
            return Response.status(500).entity("Error while stopping rebalancer: " + e.getMessage()).build();
        }
    }


    @POST
    @Path("/rebalance")
    public Response rebalance(@QueryParam("appName") String appName) {
        try {
            ProcessingUnitInstance instance = findRebalancerInstance(admin, appName);
            if (instance == null) {
                return Response.status(404).entity("Rebalancer not found").build();
            }

            instance.getSpaceInstance().getGigaSpace().write(new SingleRebalanceEvent());
            logger.info("Object SINGLE REBALANCE saved to space");
            return Response.ok().entity("Rebalancing initiated").build();

        } catch (Throwable e) {
            logger.error("Error while initiating rebalancing", e);
            return Response.status(500).entity("Error while initiating rebalancing: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/rebalance")
    public Response getGridState(@QueryParam("appName") String appName) {
        try {
            ProcessingUnitInstance instance = findRebalancerInstance(admin, appName);
            if (instance == null) {
                return Response.status(404).entity("Rebalancer not found").build();
            }


            instance.getSpaceInstance().getGigaSpace().write(new GridStateEvent());
            logger.info("Object STATE saved to space");

            GridStateEvent result = instance.getSpaceInstance().getGigaSpace().take(
                    new SQLQuery<>(GridStateEvent.class, "processed = true"), TIMEOUT);

            if (result == null) {
                return Response.status(404).entity("Failed to get state from grid").build();
            }

            return Response.ok().entity("Grid balanced = " + result.isBalanced()).build();


        } catch (Throwable e) {
            logger.error("Error while reading grid state", e);
            return Response.status(500).entity("Error while reading grid state: " + e.getMessage()).build();
        }
    }

}
