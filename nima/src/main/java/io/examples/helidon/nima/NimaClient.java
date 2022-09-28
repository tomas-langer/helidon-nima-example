package io.examples.helidon.nima;

import io.helidon.nima.webclient.http1.Http1Client;
import io.helidon.nima.webclient.http1.Http1ClientResponse;

public class NimaClient {
    public static void main(String[] args) {
        Http1Client client = Http1Client.builder()
                .build();

        try (Http1ClientResponse response = client.get()
                .uri("http://localhost:8080/one")
                .request()) {

            System.out.println(response.status());
            System.out.println(response.entity().as(String.class));
        }
    }
}
