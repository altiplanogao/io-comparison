package being.altiplano.ioservice;

import being.altiplano.config.Command;
import being.altiplano.config.Reply;

import java.io.IOException;

/**
 * Created by gaoyuan on 01/03/2017.
 */
public abstract class ConnectionClient extends AbstractClient {
    private IClientAccess clientAccess;

    public ConnectionClient(String address, int port) {
        super(address, port);
    }

    @Override
    public final void connect() throws IOException, InterruptedException {
        close();
        doConnect();
        clientAccess = createAccess();
    }

    protected abstract void doConnect() throws IOException, InterruptedException;

    protected abstract IClientAccess createAccess() throws IOException;

    @Override
    public final void disConnect() throws IOException, InterruptedException {
        clientAccess = null;
        doDisConnect();
    }

    protected abstract void doDisConnect() throws IOException, InterruptedException;

    protected <T extends Reply> T writeAndRead(Command command) throws IOException {
        try {
            synchronized (clientAccess) {
                clientAccess.writeCommand(command);
                Reply reply = clientAccess.readReply();
                return (T) reply;
            }
        } finally {
        }
    }
}
