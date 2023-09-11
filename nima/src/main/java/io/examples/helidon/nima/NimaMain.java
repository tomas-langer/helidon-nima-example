package io.examples.helidon.nima;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.helidon.config.Config;
import io.helidon.http.Http;
import io.helidon.http.InternalServerException;
import io.helidon.webclient.api.WebClient;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class NimaMain {
    private static final Http.Header SERVER = Http.Headers.create(Http.HeaderNames.SERVER, "Nima");
    private static final AtomicInteger COUNTER = new AtomicInteger();
    // no need to use secure random to compute sleep times
    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        Config config = Config.create();

        WebServer ws = WebServer.builder()
                .routing(NimaMain::routing)
                .config(config.get("server"))
                .build()
                .start();

        BlockingService.client(WebClient.builder()
                                       .baseUri("http://localhost:" + ws.port())
                                       .build());
    }

    static void routing(HttpRouting.Builder rules) {
        rules.addFilter((chain, req, res) -> {
                    res.header(SERVER);
                    chain.proceed();
                })
                .get("/remote", NimaMain::remote)
                .register("/", new BlockingService());
    }

    private static void remote(ServerRequest req, ServerResponse res) {
        // the remote service will randomly sleep up to half a second
        int sleepMillis = RANDOM.nextInt(500);
        int counter = COUNTER.incrementAndGet();

        try {
            TimeUnit.MILLISECONDS.sleep(sleepMillis);
        } catch (InterruptedException e) {
            throw new InternalServerException("Failed to sleep", e);
        }
        res.send("remote_" + counter);
    }
}
