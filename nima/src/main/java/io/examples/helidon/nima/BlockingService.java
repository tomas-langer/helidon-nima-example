package io.examples.helidon.nima;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.helidon.common.http.InternalServerException;
import io.helidon.nima.webclient.http1.Http1Client;
import io.helidon.nima.webserver.http.HttpRules;
import io.helidon.nima.webserver.http.HttpService;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.nima.webserver.http.ServerResponse;

class BlockingService implements HttpService {
    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

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
                .get("/parallel", this::parallel);
    }

    private static Http1Client client() {
        if (client == null) {
            throw new RuntimeException("Client must be configured on BlockingService");
        }
        return client;
    }

    private void one(ServerRequest req, ServerResponse res) {
        String response = callRemote(client());

        res.send(response);
    }

    private void parallel(ServerRequest req, ServerResponse res) {
        int count = count(req);

        try {
            parallel(count, res);
        } catch (InterruptedException | ExecutionException e) {
            throw new InternalServerException("Failed to execute asynchronous tasks", e);
        }
    }

    private void parallel(int count, ServerResponse res) throws ExecutionException, InterruptedException {
        List<String> responses = new LinkedList<>();

        List<Callable<String>> callables = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            callables.add(() -> callRemote(client));
        }

        for (Future<String> future : EXECUTOR.invokeAll(callables)) {
            responses.add(future.get());
        }
        res.send("Combined results: " + responses);
    }

    private void sequence(ServerRequest req, ServerResponse res) {
        int count = count(req);
        List<String> responses = new LinkedList<>();

        for (int i = 0; i < count; i++) {
            responses.add(callRemote(client));
        }

        res.send("Combined results: " + responses);
    }

    private static String callRemote(Http1Client client) {
        return client.get().path("/remote").request(String.class);
    }

    private int count(ServerRequest req) {
        return req.query().first("count").map(Integer::parseInt).orElse(3);
    }
}
