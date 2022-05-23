package being.altiplano.commumication.protocol;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

public abstract class Server<REQUEST, RESPONSE> implements Closeable {
    private final Listenable<REQUEST> requestListenable = new Listenable<>();

    private final Listenable<Pair<REQUEST, RESPONSE>> responseListenable = new Listenable<>();

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
        responseListenable.fire(new Pair<>(request, response));
        return response;
    }

    public final void addRequestListener(Listener<REQUEST> requestListener) {
        requestListenable.addListener(requestListener);
    }

    public final boolean removeRequestListener(Listener<REQUEST> requestListener) {
        return requestListenable.removeListener(requestListener);
    }

    public final void addResponseListener(Listener<Pair<REQUEST, RESPONSE>> responseListener) {
        responseListenable.addListener(responseListener);
    }

    public final boolean removeResponseListener(Listener<Pair<REQUEST, RESPONSE>> responseListener) {
        return responseListenable.removeListener(responseListener);
    }
}
