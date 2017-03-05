package being.altiplano.ioservice.bio;

import being.altiplano.ioservice.ConnectionClient;
import being.altiplano.ioservice.IClientAccess;

import java.io.IOException;
import java.net.Socket;

/**
 * Implementation of {@link being.altiplano.ioservice.IClient} using BIO
 */
public class BioClient extends ConnectionClient {
    private volatile Socket socket;

    public BioClient(String address, int port) {
        super(address, port);
    }

    @Override
    protected void doConnect() throws IOException, InterruptedException {
        socket = new Socket(address, port);
    }

    @Override
    protected void doDisConnect() throws IOException, InterruptedException {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    @Override
    protected IClientAccess createAccess() throws IOException {
        return new BioClientAccess(socket);
    }
}
