package io.examples.helidon.nima;

import io.helidon.nima.testing.junit5.webserver.DirectClient;
import io.helidon.nima.testing.junit5.webserver.RoutingTest;

// test routing only, does not open socket
@RoutingTest
class BlockingServiceTest extends AbstractBlockingServiceTest {
    BlockingServiceTest(DirectClient client) {
        super(client);
    }
}
