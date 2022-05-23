package being.altiplano.commumication.io;

import being.altiplano.commumication.protocol.Deserializer;
import being.altiplano.commumication.protocol.GenericServer;
import being.altiplano.commumication.protocol.Serializer;
import being.altiplano.commumication.protocol.Server;

import java.util.function.Function;

public class ServerFactory {
    public static <Q, A> Server<Q, A> create(int port, int magic, int frameSize,
                                             Deserializer<Q> requestDeserializer,
                                             Serializer<A> responseSerializer,
                                             Function<Q, A> processor) {
        return new GenericServer<>(() -> new RawServer(port, magic, frameSize),
                requestDeserializer,
                responseSerializer)
                .setProcessor(processor);
    }
}
