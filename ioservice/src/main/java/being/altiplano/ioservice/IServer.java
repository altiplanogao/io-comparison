package being.altiplano.ioservice;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by gaoyuan on 23/02/2017.
 */
public interface IServer extends Closeable {
    void start() throws IOException, InterruptedException;

    void stop(boolean waitDone) throws IOException, InterruptedException;
}
