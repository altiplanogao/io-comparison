package being.altiplano.ioservice;

import java.io.IOException;

/**
 * Created by gaoyuan on 23/02/2017.
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
