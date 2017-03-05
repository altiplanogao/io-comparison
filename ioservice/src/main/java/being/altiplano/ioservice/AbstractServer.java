package being.altiplano.ioservice;

import java.io.IOException;

/**
 * Abstract server
 */
public abstract class AbstractServer implements IServer {
    protected final int port;

    public AbstractServer(int port) {
        this.port = port;
    }


    @Override
    public final void close() throws IOException {
        try {
            stop(true);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}
