package com.gigaspaces.gigapro;

import com.j_spaces.core.client.SQLQuery;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.lang.math.NumberUtils;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.pu.ProcessingUnitInstance;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * @author Denys_Novikov
 * Date: 05.04.2018
 */
public class Controller {

    private static final String PARAME_NAME = "appName=";
    private static final int TIMEOUT = 10000; // 10 sec

    private static Admin admin;

    public static void main(String[] args) {

        if (invalidInputParams(args)) return;

        int port = Integer.parseInt(args[0]);
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            System.out.println("Failed to create httpServer, " + e.getMessage());
            System.out.println("Check that port available and restart the app.");
            return;
        }
        server.createContext("/v1/controller/start", new StartRebalancerHandler());
        server.createContext("/v1/controller/stop", new StopRebalancerHandler());
        server.createContext("/v1/controller/rebalance", new SingleRebalanceHandler());
        System.out.println("Starting server on port " + port);

        admin = getAdmin(args[1]);
        server.start();
    }

    private static boolean invalidInputParams(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Port cannot be null");
            return true;
        }

        if (!NumberUtils.isDigits(args[0])) {
            System.out.println("Port must be a number");
            return true;
        }

        if (args.length == 1) {
            System.out.println("Lookup locator cannot be null");
            return true;
        }
        return false;
    }


    private static class StartRebalancerHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String appName = parseAppName(exchange);
            ProcessingUnitInstance rebalancer = InstanceUtil.findRebalancerInstance(admin, appName);
            if (rebalancer == null) {
                responseNotFound(exchange, "Rebalancer not found");
                return;
            }
            rebalancer.getSpaceInstance().getGigaSpace().write(new RebalancerStartEvent());
            responseOk(exchange, "Rebalancer started");

        }
    }

    private static class StopRebalancerHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String appName = parseAppName(exchange);
            ProcessingUnitInstance rebalancer = InstanceUtil.findRebalancerInstance(admin, appName);
            if (rebalancer == null) {
                responseNotFound(exchange, "Rebalancer not found");
                return;
            }
            rebalancer.getSpaceInstance().getGigaSpace().write(new RebalancerStopEvent());
            responseOk(exchange,"Rebalancer stopped");

        }
    }

    private static String parseAppName(HttpExchange exchange) {
        return exchange.getRequestURI().getQuery().substring(exchange.getRequestURI().getQuery().indexOf(PARAME_NAME) + PARAME_NAME.length());
    }

    private static class SingleRebalanceHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String appName = parseAppName(exchange);
            ProcessingUnitInstance rebalancer = InstanceUtil.findRebalancerInstance(admin, appName);
            if (rebalancer == null) {
                responseNotFound(exchange, "Rebalancer not found");
                return;
            }


            String httpMethod = exchange.getRequestMethod();

            switch (httpMethod) {
                case "GET":
                    rebalancer.getSpaceInstance().getGigaSpace().write(new GridStateEvent());
                    GridStateEvent result = rebalancer.getSpaceInstance().getGigaSpace().take(
                            new SQLQuery<>(GridStateEvent.class, "processed = true"), TIMEOUT);

                    if (result == null) {
                        responseNotFound(exchange, "Failed to get state from grid");
                        return;
                    }
                    responseOk(exchange, "Grid balanced = " + result.isBalanced());
                    break;

                case "POST":
                    rebalancer.getSpaceInstance().getGigaSpace().write(new SingleRebalanceEvent());
                    responseOk(exchange, "Single rebalancing initiated");
                    break;

                default:
                    writeResponse(exchange, "HTTP method not supported", 405);
            }
        }
    }

    private static void responseNotFound(HttpExchange exchange, String message) throws IOException {
        writeResponse(exchange, message, 404);
    }

    private static void responseOk(HttpExchange exchange, String message) throws IOException {
        writeResponse(exchange, message, 200);
    }

    private static void writeResponse(HttpExchange exchange, String message, int code) throws IOException {
        byte[] bytes = message.getBytes();
        exchange.sendResponseHeaders(code, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static Admin getAdmin(String lookupLocator) {
        return new AdminFactory().addLocator(lookupLocator).createAdmin();
    }

}
