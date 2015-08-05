package org.madrona.http.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ConnectionCheckerImpl implements ConnectionChecker {

    private static final Logger LOGGER = LogManager.getLogger(ConnectionCheckerImpl.class);

    private String host;

    private int port;

    private ScheduledExecutorService connectionCheckExecutor = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> scheduledFuture;

    private boolean isConnectionCheckerRunning = false;



    @Override
    public void startChecking(String host, int port) {
        if (!isConnectionCheckerRunning) {
            LOGGER.debug("Starting connection check scheduler");
            this.host = host;
            this.port = port;
            isConnectionCheckerRunning = true;
            scheduledFuture = connectionCheckExecutor.scheduleAtFixedRate(new ConnectionCheckTask(), 0, 1, TimeUnit.SECONDS);
        }

    }

    private void stopScheduler() {
        LOGGER.debug("Shutting down the connection check scheduler");
        scheduledFuture.cancel(true);
        isConnectionCheckerRunning = false;
    }

    private class ConnectionCheckTask implements Runnable {
        @Override
        public void run() {
            LOGGER.debug("Checking whether the connection is available ");
            /*HttpClient client = new HttpClientImpl();
            client.init(responseHandler);
            boolean connected = client.isConnected(host, port);
            LOGGER.debug("Connection status : " + connected);
            if (connected) {
                client.createConnections(host, port, 3);
                stopScheduler();
            }*/
        }
    }
}
