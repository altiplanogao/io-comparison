package being.altiplano.commumication.protocol;

import java.io.Closeable;
import java.io.IOException;

public interface Client<REQUEST, RESPONSE> extends Closeable {
    void start() throws InterruptedException;

    void stop() throws InterruptedException;

    default void close() throws IOException {
        try {
            stop();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    void request(REQUEST request);

    void registerResponseListener(Listener<RESPONSE> responseListener);
}