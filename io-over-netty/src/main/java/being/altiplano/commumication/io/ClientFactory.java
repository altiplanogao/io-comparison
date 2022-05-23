package being.altiplano.commumication.io;

import being.altiplano.commumication.protocol.Client;
import being.altiplano.commumication.protocol.Deserializer;
import being.altiplano.commumication.protocol.GenericClient;
import being.altiplano.commumication.protocol.Serializer;

public class ClientFactory {
    public static <Q, A> Client<Q, A> create(String address, int port, int magic, int frameSize,
                                             Serializer<Q> requestSerializer,
                                             Deserializer<A> responseDeserializer) {
        return new GenericClient<>(() -> new RawClient( address, port, magic, frameSize),
                 requestSerializer,
                 responseDeserializer);
    }
}
