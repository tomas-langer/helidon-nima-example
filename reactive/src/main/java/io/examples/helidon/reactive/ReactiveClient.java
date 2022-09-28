package io.examples.helidon.reactive;

import io.helidon.reactive.webclient.WebClient;

public class ReactiveClient {
    public static void main(String[] args) {
        WebClient client = WebClient.create();

        client.get()
                .uri("http://localhost:8080/one")
                .request()
                .forSingle(response -> {
                    System.out.println(response.status());
                    response.content().as(String.class)
                                    .forSingle(System.out::println);
                });
    }
}
