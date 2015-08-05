package hms.globe.connector.http;

import hms.globe.connector.http.client.ClientHandler;
import hms.globe.connector.http.client.HttpClient;
import hms.globe.connector.http.client.NettyClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.*;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class ClientRunner {

    private static final Logger LOGGER = LogManager.getLogger(ClientRunner.class);

    private static final int SERVER_PORT = 1234;

    private static final String SERVER_HOST = "127.0.0.1";

    private static final AtomicInteger counter = new AtomicInteger();

    private ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(2);

    private HttpClient client;


    public void init(final String host, final int port) {
        client = new NettyClient(new DummyClientHandler());
        client.createConnection(host, port, 3);
    }

    private void send(String message) {
        counter.incrementAndGet();
        client.send(buildHttpRequest(message));
    }

    private static HttpRequest buildHttpRequest(String message) {
        String uri = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/" + message;

        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);
        request.setHeader(HttpHeaders.Names.HOST, SERVER_HOST);
        request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
        return request;
    }

    public static void main(String[] args) {
        final ClientRunner runner = new ClientRunner();

        runner.init(SERVER_HOST, SERVER_PORT);

        runner.send("message");

        /*runner.executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                runner.send("message");
            }
        }, 1, 1, TimeUnit.SECONDS);*/

    }


    public class DummyClientHandler implements ClientHandler {

        @Override
        public void messageReceived(HttpResponse response) {
            LOGGER.debug("==================RESPONSE RECEIVED=========================");
            LOGGER.info("Response [{}] received from the server" , response);
        }

        @Override
        public void notifyError() {
            LOGGER.error("Error occurred in client");
        }
    }
}
