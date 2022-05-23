package being.altiplano.commumication.protocol;

import being.altiplano.commumication.io.ClientFactory;
import being.altiplano.commumication.io.ServerFactory;
import being.altiplano.commumication.mock.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void simpleResponseCount() throws InterruptedException, IOException {
        try (Server<MRequest, MResponse> server = createServer(port);
             Client<MRequest, MResponse> client = createClient(port)){
            server.start();
            client.start();

            AtomicInteger serverRawRequestCounter = new AtomicInteger(0);
            AtomicInteger serverRawResponseCounter = new AtomicInteger(0);
            AtomicInteger serverRequestCounter = new AtomicInteger(0);
            AtomicInteger serverResponseCounter = new AtomicInteger(0);
            server.addRequestListener(event -> serverRequestCounter.incrementAndGet())
                    .addResponseListener(event -> serverResponseCounter.incrementAndGet());
            if(server instanceof GenericServer) {
                GenericServer<MRequest, MResponse> genericServer = (GenericServer<MRequest, MResponse>)server;
                Server<byte[], byte[]> innerServer = genericServer.getInnerServer();
                innerServer.addRequestListener(event -> serverRawRequestCounter.incrementAndGet());
                innerServer.addResponseListener(event -> serverRawResponseCounter.incrementAndGet());
            }

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
            client.addResponseListener(event -> {
                if ("xyz".equals(event.getData())){
                    waitResponse.countDown();
                }
            });
            for (MRequest request : requests) {
                client.request(request);
            }

            waitResponse.await();

            Assertions.assertEquals(8, serverRawRequestCounter.get());
            Assertions.assertEquals(8, serverRawResponseCounter.get());
            Assertions.assertEquals(8, serverRequestCounter.get());
            Assertions.assertEquals(8, serverResponseCounter.get());

            client.stop();
            server.stop(true);
        }
    }
}