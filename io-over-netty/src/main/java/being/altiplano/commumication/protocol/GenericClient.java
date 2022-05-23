package being.altiplano.commumication.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericClient<REQUEST, RESPONSE> implements Client<REQUEST, RESPONSE> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericClient.class);

    private final Client<byte[], byte[]> innerClient;

    private final Serializer<REQUEST> requestSerializer;

    private final Deserializer<RESPONSE> responseDeserializer;

    public GenericClient(Client<byte[], byte[]> innerClient,
                         Serializer<REQUEST> requestSerializer,
                         Deserializer<RESPONSE> responseDeserializer) {
        this.innerClient = innerClient;
        this.requestSerializer = requestSerializer;
        this.responseDeserializer = responseDeserializer;
    }

    @Override
    public void start() throws InterruptedException {
        innerClient.start();
    }

    @Override
    public void stop() throws InterruptedException {
        innerClient.stop();
    }

    @Override
    public void request(REQUEST request) {
        LOGGER.info("make request");
        byte[] reqInBytes = requestSerializer.serialize(request);
        innerClient.request(reqInBytes);
    }

    @Override
    public void registerResponseListener(Listener<RESPONSE> listener) {
        innerClient.registerResponseListener(new RawResponseListener(listener));
    }

    private class RawResponseListener implements Listener<byte[]> {
        final Listener<RESPONSE> listener;

        private RawResponseListener(Listener<RESPONSE> listener) {
            this.listener = listener;
        }

        @Override
        public void onEvent(byte[] responseInBytes) {
            RESPONSE response = responseDeserializer.deserialize(responseInBytes);
            listener.onEvent(response);
        }
    }
}
