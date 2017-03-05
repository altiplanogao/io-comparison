package being.altiplano.ioservice;

import java.io.Closeable;
import java.io.IOException;

/**
 * A socket server
 */
public interface IServer extends Closeable {
    void start() throws IOException, InterruptedException;

    void stop(boolean waitDone) throws IOException, InterruptedException;
}
