package being.altiplano.commumication.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Supplier;

public class GenericServer<REQUEST, RESPONSE> extends Server<REQUEST, RESPONSE> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericServer.class);

    private final Server<byte[], byte[]> innerServer;

    private final Deserializer<REQUEST> requestDeserializer;

    private final Serializer<RESPONSE> responseSerializer;

    public GenericServer(Supplier<Server<byte[], byte[]>> innerServerSupplier,
                         Deserializer<REQUEST> requestDeserializer,
                         Serializer<RESPONSE> responseSerializer) {
        super();
        this.innerServer = innerServerSupplier.get();
        this.requestDeserializer = requestDeserializer;
        this.responseSerializer = responseSerializer;
    }

    @Override
    public Server<REQUEST, RESPONSE> setProcessor(final Function<REQUEST, RESPONSE> processor) {
        super.setProcessor(processor);
        Function<byte[], byte[]> innerProcessor = requestBytes -> {
            REQUEST req = requestDeserializer.deserialize(requestBytes);
            LOGGER.info("server: got request");
            RESPONSE res = processRequest(req);
            LOGGER.info("server: response prepared");
            if (res == null) {
                return null;
            }
            return responseSerializer.serialize(res);
        };
        this.innerServer.setProcessor(innerProcessor);
        return this;
    }

    public void start() throws InterruptedException {
        innerServer.start();
    }

    public void stop(boolean waitDone) throws InterruptedException {
        innerServer.stop(waitDone);
    }
}
