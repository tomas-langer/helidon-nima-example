package io.examples.helidon.reactive;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import io.helidon.common.reactive.Multi;
import io.helidon.common.reactive.Single;
import io.helidon.reactive.faulttolerance.Async;
import io.helidon.reactive.webclient.WebClient;
import io.helidon.reactive.webserver.Routing;
import io.helidon.reactive.webserver.ServerRequest;
import io.helidon.reactive.webserver.ServerResponse;
import io.helidon.reactive.webserver.Service;

class ReactiveService implements Service {
    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private static final Async ASYNC = Async.builder().executor(EXECUTOR).build();

    // we use this approach as we are calling the same service
    // in a real application, we would use DNS resolving, or k8s service names
    private static WebClient client;

    static void port(int port) {
        client = WebClient.builder()
                .baseUri("http://localhost:" + port + "/remote")
                .build();
    }

    @Override
    public void update(Routing.Rules rules) {
        rules.get("/one", this::one)
                .get("/sequence", this::sequence)
                .get("/parallel", this::parallel)
                .get("/sleep", this::sleep);
    }

    private static WebClient client() {
        if (client == null) {
            throw new RuntimeException("Port must be configured on NonBlockingService");
        }
        return client;
    }

    private void sleep(ServerRequest req, ServerResponse res) {
        ASYNC.invoke(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        return "failed: " + e;
                    }
                    return "finished";
                })
                .forSingle(res::send)
                .exceptionally(res::send);
    }

    private void one(ServerRequest req, ServerResponse res) {
        Single<String> response = client.get()
                .request(String.class);

        response.forSingle(res::send)
                .exceptionally(res::send);
    }

    private void sequence(ServerRequest req, ServerResponse res) {
        int count = count(req);

        Multi.range(0, count)
                .flatMap(i -> client.get().request(String.class))
                .collectList()
                .map(it -> "Combined results: " + it)
                .onError(res::send)
                .forSingle(res::send);
    }

    private void parallel(ServerRequest req, ServerResponse res) {
        int count = count(req);

        Multi.range(0, count)
                .flatMap(i -> Single.create(CompletableFuture.supplyAsync(() -> {
                            return client().get().request(String.class);
                        }, EXECUTOR))
                        .flatMap(Function.identity()))
                .collectList()
                .map(it -> "Combined results: " + it)
                .onError(res::send)
                .forSingle(res::send);
    }

    private int count(ServerRequest req) {
        return req.queryParams().first("count").map(Integer::parseInt).orElse(3);
    }
}
