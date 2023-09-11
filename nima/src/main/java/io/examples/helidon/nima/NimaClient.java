package io.examples.helidon.nima;

import io.helidon.webclient.api.HttpClientResponse;
import io.helidon.webclient.api.WebClient;

public class NimaClient {
    public static void main(String[] args) {
        WebClient client = WebClient.create();

        try (HttpClientResponse response = client.get()
                .uri("http://localhost:8080/one")
                .request()) {

            System.out.println(response.status());
            System.out.println(response.entity().as(String.class));
        }
    }
}
