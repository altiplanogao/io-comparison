package being.altiplano.commumication.protocol;

import being.altiplano.commumication.mock.MCmdCode;
import being.altiplano.commumication.mock.MRequest;
import being.altiplano.commumication.mock.MResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

class ServerClientTest extends ServerClientTestBase {
    @Test
    public void testAddListener() {
        testWithServerAndClient((server, client) -> {
            AtomicInteger serverRawRequestCounter = new AtomicInteger(0);
            AtomicInteger serverRawResponseCounter = new AtomicInteger(0);
            AtomicInteger serverRequestCounter = new AtomicInteger(0);
            AtomicInteger serverResponseCounter = new AtomicInteger(0);
            server.addRequestListener(event -> serverRequestCounter.incrementAndGet())
                    .addResponseListener(event -> serverResponseCounter.incrementAndGet());
            if (server instanceof GenericServer) {
                GenericServer<MRequest, MResponse> genericServer = (GenericServer<MRequest, MResponse>) server;
                Server<byte[], byte[]> innerServer = genericServer.getInnerServer();
                innerServer.addRequestListener(event -> serverRawRequestCounter.incrementAndGet());
                innerServer.addResponseListener(event -> serverRawResponseCounter.incrementAndGet());
            }

            fireRequestsAndWaitResponse(client);

            Assertions.assertEquals(8, serverRawRequestCounter.get());
            Assertions.assertEquals(8, serverRawResponseCounter.get());
            Assertions.assertEquals(8, serverRequestCounter.get());
            Assertions.assertEquals(8, serverResponseCounter.get());
        });
    }

    private void fireRequestsAndWaitResponse(Client<MRequest, MResponse> client) throws InterruptedException {
        int snGen = 0;
        MRequest[] requests = new MRequest[]{
                new MRequest(snGen++, MCmdCode.UPPER, "Abc"),
                new MRequest(snGen++, MCmdCode.LOWER, "Abc"),
                new MRequest(snGen++, MCmdCode.ECHO, "abc"),
                new MRequest(snGen++, MCmdCode.ECHO_TWICE, "abc"),
                new MRequest(snGen++, MCmdCode.REVERSE, "abc"),
                new MRequest(snGen++, MCmdCode.COUNT, "abc"),
                new MRequest(snGen++, MCmdCode.NO_REPLY, "abc"),
                new MRequest(snGen, MCmdCode.ECHO, "xyz"),
        };
        CountDownLatch waitResponse = new CountDownLatch(1);
        final Listener<MResponse> waitResponseListener = event -> {
            if ("xyz".equals(event.getData())) {
                waitResponse.countDown();
            }
        };
        client.addResponseListener(waitResponseListener);
        for (MRequest request : requests) {
            client.request(request);
        }

        waitResponse.await();
        client.removeResponseListener(waitResponseListener);
    }

    @Test
    public void testRemoveListener() {
        testWithServerAndClient((server, client) -> {
            AtomicInteger serverRawRequestCounter = new AtomicInteger(0);
            AtomicInteger serverRawResponseCounter = new AtomicInteger(0);
            AtomicInteger serverRequestCounter = new AtomicInteger(0);
            AtomicInteger serverResponseCounter = new AtomicInteger(0);

            Listener<MRequest> requestListener = event -> serverRequestCounter.incrementAndGet();
            Listener<Pair<MRequest, MResponse>> responseListener = event -> serverResponseCounter.incrementAndGet();
            Listener<byte[]> rawRequestListener = event -> serverRawRequestCounter.incrementAndGet();
            Listener<Pair<byte[], byte[]>> rawResponseListener = event -> serverRawResponseCounter.incrementAndGet();

            GenericServer<MRequest, MResponse> genericServer = (GenericServer<MRequest, MResponse>) server;
            Server<byte[], byte[]> innerServer = genericServer.getInnerServer();

            server.addRequestListener(requestListener)
                    .addResponseListener(responseListener);
            innerServer.addRequestListener(rawRequestListener)
                    .addResponseListener(rawResponseListener);

            fireRequestsAndWaitResponse(client);

            Assertions.assertEquals(8, serverRawRequestCounter.get());
            Assertions.assertEquals(8, serverRawResponseCounter.get());
            Assertions.assertEquals(8, serverRequestCounter.get());
            Assertions.assertEquals(8, serverResponseCounter.get());

            server.removeRequestListener(requestListener);
            server.removeResponseListener(responseListener);
            innerServer.removeRequestListener(rawRequestListener);
            innerServer.removeResponseListener(rawResponseListener);
            fireRequestsAndWaitResponse(client);

            Assertions.assertEquals(8, serverRawRequestCounter.get());
            Assertions.assertEquals(8, serverRawResponseCounter.get());
            Assertions.assertEquals(8, serverRequestCounter.get());
            Assertions.assertEquals(8, serverResponseCounter.get());
        });
    }
}