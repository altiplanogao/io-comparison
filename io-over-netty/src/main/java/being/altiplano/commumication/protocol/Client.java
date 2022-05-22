package being.altiplano.commumication.protocol;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

public abstract class Client<REQUEST, RESPONSE> implements Closeable {
    private final Class<REQUEST> requestDataType;
    private final Function<REQUEST, byte[]> requestSerializer;

    private final Class<RESPONSE> responseDataType;
    private final Function<byte[], RESPONSE> responseDeserializer;

    private final Listenable<RESPONSE> responseListenable = new Listenable<>();

    public Client(Class<REQUEST> requestDataType, Function<REQUEST, byte[]> requestSerializer,
                  Class<RESPONSE> responseDataType, Function<byte[], RESPONSE> responseDeserializer) {
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

    protected void onReceiveRawResponse(byte[] rawResponse){
        RESPONSE response = responseDeserializer.apply(rawResponse);
        responseListenable.fire(response);
    }

    public Listenable<RESPONSE> getResponseListenable(){
        return responseListenable;
    }
}
