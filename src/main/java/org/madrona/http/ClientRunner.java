package org.madrona.http;

import org.madrona.http.client.NettyClient;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ClientRunner {

    private static final int PORT = 8082;

    private static final String HOST = "127.0.0.1";

    public static void main(String[] args) throws InterruptedException {
        // Initializing http client
        NettyClient client = new NettyClient();
        client.init(HOST, PORT);
        Thread.sleep(2000);
//        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(2);

   /*     executorService.scheduleAtFixedRate(() -> {
            client.send("http://" + HOST + ":" + PORT + "/hello");
        }, 1000, 2000, TimeUnit.MILLISECONDS);*/

        client.send("http://" + HOST + ":" + PORT );
        Thread.sleep(100l);

        client.send("http://" + HOST + ":" + PORT );
        Thread.sleep(100l);


        client.send("http://" + HOST + ":" + PORT);
        Thread.sleep(100l);



        Thread.sleep(10000l);

//        executorService.shutdown();
        client.shutdown();
    }
}
