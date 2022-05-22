package being.altiplano.commumication.protocol;

public abstract class Server {
    protected final int port;

    public Server(int port) {
        this.port = port;
    }

    public abstract void start() throws InterruptedException;

    public abstract void stop(boolean waitDone) throws InterruptedException;

}
