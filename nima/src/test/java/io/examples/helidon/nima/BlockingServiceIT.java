package io.examples.helidon.nima;

import io.helidon.webclient.api.WebClient;
import io.helidon.webserver.testing.junit5.ServerTest;

// test proper HTTP communication, opens socket
@ServerTest
class BlockingServiceIT extends AbstractBlockingServiceTest {
    BlockingServiceIT(WebClient client) {
        super(client);
    }
}
