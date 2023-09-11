package io.examples.helidon.nima;

import io.helidon.webclient.api.WebClient;
import io.helidon.webserver.testing.junit5.RoutingTest;

// test routing only, does not open socket
@RoutingTest
class BlockingServiceTest extends AbstractBlockingServiceTest {
    BlockingServiceTest(WebClient client) {
        super(client);
    }
}
