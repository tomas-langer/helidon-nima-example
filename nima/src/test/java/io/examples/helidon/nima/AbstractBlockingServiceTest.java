package io.examples.helidon.nima;

import java.util.Arrays;

import io.helidon.http.Http;
import io.helidon.webclient.api.ClientResponseTyped;
import io.helidon.webclient.api.WebClient;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

abstract class AbstractBlockingServiceTest {
    private final WebClient client;

    protected AbstractBlockingServiceTest(WebClient client) {
        this.client = client;
        BlockingService.client(client);
    }

    @SetUpRoute
    static void routing(HttpRouting.Builder builder) {
        NimaMain.routing(builder);
    }

    @Test
    void testOne() {
        ClientResponseTyped<String> response = client.get("/one")
                .request(String.class);

        assertThat(response.status(), is(Http.Status.OK_200));
        assertThat(response.entity(), startsWith("remote_"));
    }

    @Test
    void testSequence() {
        ClientResponseTyped<String> response = client.get("/sequence")
                .request(String.class);

        assertThat(response.status(), is(Http.Status.OK_200));
        String entity = response.entity();
        int[] results = splitAndValidateEntity(entity);
        validateUnique(results);
        validateSequence(results);
        validateOrdered(results);
    }

    @Test
    void testSequenceParam() {
        ClientResponseTyped<String> response = client.get("/sequence")
                .queryParam("count", "11")
                .request(String.class);
        assertThat(response.status(), is(Http.Status.OK_200));
        String entity = response.entity();
        int[] results = splitAndValidateEntity(entity);
        validateUnique(results);
        validateSequence(results);
        validateOrdered(results);
    }

    @Test
    void testParallel() {
        ClientResponseTyped<String> response = client.get("/parallel")
                .request(String.class);

        assertThat(response.status(), is(Http.Status.OK_200));
        String entity = response.entity();
        int[] results = splitAndValidateEntity(entity);
        validateUnique(results);
        validateSequence(results);
    }

    @Test
    void testParallelParam() {
        ClientResponseTyped<String> response = client.get("/parallel")
                .queryParam("count", "11")
                .request(String.class);
        assertThat(response.status(), is(Http.Status.OK_200));
        String entity = response.entity();
        int[] results = splitAndValidateEntity(entity);
        validateUnique(results);
        validateSequence(results);
    }

    private void validateOrdered(int[] results) {
        int[] copy = Arrays.copyOf(results, results.length);
        Arrays.sort(copy);
        assertThat("Result should be sequential and ordered", results, is(copy));
    }

    private void validateSequence(int[] results) {
        Arrays.sort(results);
        int last = -1;
        for (int result : results) {
            assertThat(result, not(-1));
            if (last == -1) {
                last = result;
            } else {
                assertThat("Results should be a sequence of numbers, but the sequence is missing a number: "
                                   + Arrays.toString(results),
                           result - last, is(1));
                last = result;
            }
        }
    }

    private void validateUnique(int[] results) {
        assertThat("Results should be distinct: " + Arrays.toString(results),
                   results.length,
                   is((int) Arrays.stream(results).boxed().count()));
    }

    private int[] splitAndValidateEntity(String entity) {
        String prefix = "Combined results: [";
        assertThat(entity, startsWith("Combined results: ["));
        assertThat(entity, endsWith("]"));
        String remotes = entity.substring(prefix.length(), entity.length() - 1);
        System.out.println(remotes);
        String[] splitRemotes = remotes.split(", ");
        int[] result = new int[splitRemotes.length];
        for (int i = 0; i < splitRemotes.length; i++) {
            String splitRemote = splitRemotes[i];
            assertThat(splitRemote, startsWith("remote_"));
            result[i] = Integer.parseInt(splitRemote.substring("remote_".length()));
        }
        return result;
    }
}
