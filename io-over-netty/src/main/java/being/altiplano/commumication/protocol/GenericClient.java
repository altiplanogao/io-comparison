package being.altiplano.commumication.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class GenericClient<REQUEST, RESPONSE> extends Client<REQUEST, RESPONSE> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericClient.class);

    private final Client<byte[], byte[]> innerClient;

    private final Serializer<REQUEST> requestSerializer;

    private final Deserializer<RESPONSE> responseDeserializer;

    public GenericClient(Supplier<Client<byte[], byte[]>> innerClientSupplier,
                         Serializer<REQUEST> requestSerializer,
                         Deserializer<RESPONSE> responseDeserializer) {
        this.innerClient = innerClientSupplier.get();
        this.innerClient.addResponseListener(this::onReceiveRawResponse);
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
        LOGGER.debug("make request");
        byte[] reqInBytes = requestSerializer.serialize(request);
        innerClient.request(reqInBytes);
    }

    protected final void onReceiveRawResponse(byte[] rawResponse) {
        RESPONSE response = responseDeserializer.deserialize(rawResponse);
        super.fireResponseReceivedEvent(response);
    }
}
