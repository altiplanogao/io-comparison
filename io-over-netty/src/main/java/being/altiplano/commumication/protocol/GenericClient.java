package being.altiplano.commumication.protocol;

import java.util.function.Function;

public class GenericClient<REQUEST, RESPONSE> implements Client<REQUEST, RESPONSE> {
    private final Client<byte[], byte[]> innerClient;

    private final Function<REQUEST, byte[]> requestSerializer;

    private final Function<byte[], RESPONSE> responseDeserializer;

    public GenericClient(Client<byte[], byte[]> innerClient,
                         Function<REQUEST, byte[]> requestSerializer,
                         Function<byte[], RESPONSE> responseDeserializer) {
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
        byte[] reqInBytes = requestSerializer.apply(request);
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
            RESPONSE response = responseDeserializer.apply(responseInBytes);
            listener.onEvent(response);
        }
    }
}
