package being.altiplano.ioservice;

/**
 * Created by gaoyuan on 23/02/2017.
 */
public abstract class AbstractServer implements IServer {
    protected final int port;

    public AbstractServer(int port) {
        this.port = port;
    }
}
