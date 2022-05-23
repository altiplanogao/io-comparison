package being.altiplano.commumication.io;

import being.altiplano.commumication.mock.*;
import being.altiplano.commumication.protocol.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Disabled
class ServerClientTest {
    private int magic = 0x11223344;
    private int frameSize = 1024;

    protected Server<MRequest, MResponse> createServer(int port) {
        return ServerFactory.create(port, magic, frameSize,
                new RequestDeserializer(), new ResponseSerializer(),
                new RequestHandler());
    }

    protected Client<MRequest, MResponse> createClient(int port) {
        return ClientFactory.create("localhost", port, magic, frameSize,
                new RequestSerializer(), new ResponseDeserializer());
    }

    @Test
    public void simpleTest() throws InterruptedException, IOException {
        int port = 9999;

        try (Server<MRequest, MResponse> server = createServer(port);
             Client<MRequest, MResponse> client = createClient(port)){
            server.start();
            client.start();
            CountDownLatch waitResponse = new CountDownLatch(1);
            AtomicReference<MResponse> responseHolder = new AtomicReference<>();
            client.registerResponseListener(event -> {
                responseHolder.set(event);
                waitResponse.countDown();
            });

            client.request(new MRequest(MCmdCode.REVERSE, "abc"));
            waitResponse.await();
            Assertions.assertEquals(new MResponse("cba"), responseHolder.get());

            client.stop();
            server.stop(true);
        }
    }
}