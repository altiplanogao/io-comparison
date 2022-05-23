package being.altiplano.commumication.protocol;

import java.io.Closeable;
import java.io.IOException;

public abstract class Client<REQUEST, RESPONSE> implements Closeable {
    private final Listenable<RESPONSE> responseListenable = new Listenable<>();

    public abstract void start() throws InterruptedException;

    public abstract void stop() throws InterruptedException;

    public final void close() throws IOException {
        try {
            stop();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public abstract void request(REQUEST request);

    public final void addResponseListener(Listener<RESPONSE> responseListener){
        responseListenable.addListener(responseListener);
    }

    public final boolean removeResponseListener(Listener<RESPONSE> responseListener) {
        return responseListenable.removeListener(responseListener);
    }

    protected final void fireResponseReceivedEvent(RESPONSE response) {
        responseListenable.fire(response);
    }
}