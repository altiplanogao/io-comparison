package being.altiplano.commumication.io;

import being.altiplano.commumication.mock.*;
import being.altiplano.commumication.protocol.Client;
import being.altiplano.commumication.protocol.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Disabled
class ServerClientTest {
    int port = 9999;
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
        try (Server<MRequest, MResponse> server = createServer(port);
             Client<MRequest, MResponse> client = createClient(port)){
            server.start();
            client.start();

            MRequest[] requests = new MRequest[]{
                    new MRequest(MCmdCode.UPPER, "Abc"),
                    new MRequest(MCmdCode.LOWER, "Abc"),
                    new MRequest(MCmdCode.ECHO, "abc"),
                    new MRequest(MCmdCode.ECHO_TWICE, "abc"),
                    new MRequest(MCmdCode.REVERSE, "abc"),
                    new MRequest(MCmdCode.COUNT, "abc"),
                    new MRequest(MCmdCode.NO_REPLY, "abc"),
                    new MRequest(MCmdCode.ECHO, "xyz"),
            };
            CountDownLatch waitResponse = new CountDownLatch(1);
            List<MResponse> responses = new ArrayList<>();
            client.addResponseListener(event -> {
                responses.add(event);
                if ("xyz".equals(event.getData())){
                    waitResponse.countDown();
                }
            });
            for (MRequest request : requests) {
                client.request(request);
            }

            MResponse[] expects = new MResponse[]{
                    new MResponse("ABC"),
                    new MResponse("abc"),
                    new MResponse( "abc"),
                    new MResponse( "abcabc"),
                    new MResponse( "cba"),
                    new MResponse( "3"),
                    new MResponse("xyz")
            };

            waitResponse.await();
            Assertions.assertArrayEquals(expects, responses.toArray(new MResponse[0]));

            client.stop();
            server.stop(true);
        }
    }
}