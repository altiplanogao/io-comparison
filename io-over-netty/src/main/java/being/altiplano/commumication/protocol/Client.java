package being.altiplano.commumication.protocol;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

public abstract class Client<REQUEST, RESPONSE> implements Closeable {
    protected final String address;
    protected final int port;
    protected final Class<REQUEST> requestDataType;
    private final Function<REQUEST, byte[]> requestSerializer;
    protected final Class<RESPONSE> responseDataType;
    protected final Function<byte[], RESPONSE> responseDeserializer;
    private final Listenable<RESPONSE> responseListenable = new Listenable<>();

    public Client(String address, int port,
                  Class<REQUEST> requestDataType, Function<REQUEST, byte[]> requestSerializer,
                  Class<RESPONSE> responseDataType, Function<byte[], RESPONSE> responseDeserializer) {
        this.address = address;
        this.port = port;
        this.requestDataType = requestDataType;
        this.requestSerializer = requestSerializer;
        this.responseDataType = responseDataType;
        this.responseDeserializer = responseDeserializer;
    }

    public abstract void start() throws InterruptedException;

    public abstract void stop() throws InterruptedException;

    public final void close() throws IOException {
        try {
            stop();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public final void request(REQUEST req) {
        byte[] reqInBytes =requestSerializer.apply(req);
        this.rawRequest(reqInBytes);
    }

    protected abstract void rawRequest(byte[] req);

    public Listenable<RESPONSE> getResponseListenable(){
        return responseListenable;
    }
}
