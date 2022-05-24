package being.altiplano.commumication.io;

import being.altiplano.commumication.mock.*;
import being.altiplano.commumication.protocol.Client;
import being.altiplano.commumication.protocol.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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

            int respSnGen = 0;
            MResponse[] expects = new MResponse[]{
                    new MResponse(respSnGen++, "ABC"),
                    new MResponse(respSnGen++, "abc"),
                    new MResponse(respSnGen++, "abc"),
                    new MResponse(respSnGen++, "abcabc"),
                    new MResponse(respSnGen++, "cba"),
                    new MResponse(respSnGen++, "3"),
                    new MResponse(respSnGen+1, "xyz")
            };

            waitResponse.await();

            Assertions.assertArrayEquals(expects, responses.toArray(new MResponse[0]));

            client.stop();
            server.stop(true);
        }
    }
}