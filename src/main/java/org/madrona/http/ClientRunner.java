package org.madrona.http;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.madrona.http.client.NettyClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;


public class ClientRunner {

    private static final Logger LOGGER = LogManager.getLogger(ClientRunner.class);

    private static final int SERVER_PORT = 8080;

    private static final String SERVER_HOST = "127.0.0.1";

    private static final AtomicInteger counter = new AtomicInteger();

    private NettyClient client;

    private static URI buildHttpRequest(String message) {
        String uri = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/" + message;

        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        final ClientRunner runner = new ClientRunner();

        runner.init(SERVER_HOST, SERVER_PORT);




    }

    public void init(final String host, final int port) {
        client = new NettyClient();
        client.init();

        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        client.send("http://" + SERVER_HOST + ":" + SERVER_PORT + "/hello");

        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        client.shutdown();
    }


}
