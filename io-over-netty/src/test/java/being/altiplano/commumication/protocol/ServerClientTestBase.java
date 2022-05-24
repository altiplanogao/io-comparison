package being.altiplano.commumication.protocol;

import being.altiplano.commumication.io.ClientFactory;
import being.altiplano.commumication.io.ServerFactory;
import being.altiplano.commumication.mock.*;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

public class ServerClientTestBase {
    int port = 9999;
    private final int magic = 0x11223344;
    private final int frameSize = 1024;

    protected Server<MRequest, MResponse> createServer(int port) {
        return ServerFactory.create(port, magic, frameSize,
                new RequestDeserializer(), new ResponseSerializer(),
                new RequestHandler());
    }

    protected Client<MRequest, MResponse> createClient(int port) {
        return ClientFactory.create("localhost", port, magic, frameSize,
                new RequestSerializer(), new ResponseDeserializer());
    }

    protected void testWithServerAndClient(CallServerAndClient method) {
        try (Server<MRequest, MResponse> server = createServer(port);
             Client<MRequest, MResponse> client = createClient(port)) {
            server.start();
            client.start();

            method.invoke(server, client);

            client.stop();
            server.stop(true);
        } catch (IOException | InterruptedException e) {
            Assertions.fail("Fail with exception");
        }
    }

    interface CallServerAndClient {
        void invoke(Server<MRequest, MResponse> server, Client<MRequest, MResponse> client) throws InterruptedException;
    }
}
