package io.examples.helidon.nima;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.helidon.nima.webclient.http1.Http1Client;
import io.helidon.nima.webserver.http.HttpRules;
import io.helidon.nima.webserver.http.HttpService;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.nima.webserver.http.ServerResponse;

class BlockingService implements HttpService {

    // we use this approach as we are calling the same service
    // in a real application, we would use DNS resolving, or k8s service names
    private static Http1Client client;

    static void client(Http1Client client) {
        BlockingService.client = client;
    }

    @Override
    public void routing(HttpRules httpRules) {
        httpRules.get("/one", this::one)
                .get("/sequence", this::sequence)
                .get("/parallel", this::parallel)
                .get("/sleep", this::sleep);
    }

    private static Http1Client client() {
        if (client == null) {
            throw new RuntimeException("Client must be configured on BlockingService");
        }
        return client;
    }

    private static String callRemote(Http1Client client) {
        return client.get()
                .path("/remote")
                .request(String.class);
    }

    private void sleep(ServerRequest req, ServerResponse res) throws Exception {
        Thread.sleep(1000);
        res.send("finished");
    }

    private void one(ServerRequest req, ServerResponse res) {
        String response = callRemote(client());

        res.send(response);
    }

    private void sequence(ServerRequest req, ServerResponse res) {
        int count = count(req);

        var responses = new ArrayList<String>();

        for (int i = 0; i < count; i++) {
            responses.add(callRemote(client));
        }

        res.send("Combined results: " + responses);
    }

    private void parallel(ServerRequest req, ServerResponse res) throws Exception {

        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            int count = count(req);

            var futures = new ArrayList<Future<String>>();
            for (int i = 0; i < count; i++) {
                futures.add(exec.submit(() -> callRemote(client)));
            }

            var responses = new ArrayList<String>();
            for (var future : futures) {
                responses.add(future.get());
            }

            res.send("Combined results: " + responses);
        }
    }

    private int count(ServerRequest req) {
        return req.query().first("count").map(Integer::parseInt).orElse(3);
    }
}
