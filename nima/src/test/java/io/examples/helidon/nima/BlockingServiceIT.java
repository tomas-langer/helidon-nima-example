package io.examples.helidon.nima;

import io.helidon.nima.testing.junit5.webserver.ServerTest;
import io.helidon.nima.webclient.http1.Http1Client;

// test proper HTTP communication, opens socket
@ServerTest
class BlockingServiceIT extends AbstractBlockingServiceTest {
    BlockingServiceIT(Http1Client client) {
        super(client);
    }
}
