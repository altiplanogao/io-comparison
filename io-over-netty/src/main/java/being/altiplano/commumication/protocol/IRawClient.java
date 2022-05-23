package being.altiplano.commumication.protocol;

public interface IRawClient {
    void start() throws InterruptedException;

    void stop() throws InterruptedException;

    void request(byte[] bytesOfRequest);

    void register(Listener<byte[]> responseListener);
}
