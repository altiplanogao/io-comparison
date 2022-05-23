package being.altiplano.commumication.protocol;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

public abstract class Server<REQUEST, RESPONSE> implements Closeable {
    private final Listenable<REQUEST> requestListenable = new Listenable<>();

    private final Listenable<RequestResponsePair<REQUEST, RESPONSE>> responseListenable = new Listenable<>();

    private Function<REQUEST, RESPONSE> processor;

    protected Server() {
    }

    public abstract void start() throws InterruptedException;

    public abstract void stop(boolean waitDone) throws InterruptedException;

    public final void close() throws IOException {
        try {
            stop(true);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public Server<REQUEST, RESPONSE> setProcessor(Function<REQUEST, RESPONSE> processor) {
        this.processor = processor;
        return this;
    }

    protected RESPONSE processRequest(REQUEST request) {
        requestListenable.fire(request);
        RESPONSE response = processor == null ? null : processor.apply(request);
        responseListenable.fire(new RequestResponsePair<>(request, response));
        return response;
    }

    public final void registerRequestListener(Listener<REQUEST> requestListener) {
        requestListenable.addListener(requestListener);
    }

    public final void registerResponseListener(Listener<RequestResponsePair<REQUEST, RESPONSE>> responseListener) {
        responseListenable.addListener(responseListener);
    }
}
