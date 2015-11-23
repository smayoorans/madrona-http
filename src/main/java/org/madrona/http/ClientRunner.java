package org.madrona.http;

import io.netty.handler.codec.http.HttpResponse;
import org.madrona.http.client.NettyClient;
import org.madrona.http.client.ResponseNotifier;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class ClientRunner {

    private static final int PORT = 8082;

    private static final String HOST = "127.0.0.1";

    public static void main(String[] args) throws InterruptedException {
        // Initializing http client

        final AtomicLong writtenMessages = new AtomicLong(0);
        final AtomicLong readMessages = new AtomicLong(0);

        NettyClient client = new NettyClient();

        client.setResponseNotifier(new ResponseNotifier() {
            @Override
            public void messageReceived(HttpResponse httpResponse) {
                readMessages.incrementAndGet() ;
            }

            @Override
            public void networkErrorOccurred(Throwable throwable) {

            }
        });
        client.init(HOST, PORT);
        Thread.sleep(2000);
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5);

        executorService.scheduleAtFixedRate(() -> {
            writtenMessages.incrementAndGet();
            client.send("http://" + HOST + ":" + PORT + "/hello");
        }, 1000, 100, TimeUnit.MICROSECONDS);


        executorService.scheduleAtFixedRate(() -> {
            System.out.println("=" + readMessages.getAndSet(0)+ "===========" + writtenMessages.getAndSet(0));
        }, 1000, 1000, TimeUnit.MILLISECONDS);

        Thread.sleep(50000l);

        executorService.shutdown();

        Thread.sleep(1000l);
        client.shutdown();
    }
}
