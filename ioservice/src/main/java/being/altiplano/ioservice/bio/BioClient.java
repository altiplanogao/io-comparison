package being.altiplano.ioservice.bio;

import being.altiplano.ioservice.AbstractClient;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by gaoyuan on 22/02/2017.
 */
public class BioClient extends AbstractClient {

    public BioClient(String address, int port) {
        super(address, port);
    }

    @Override
    public void connect() throws IOException {
        close();
        clientConnection = new BioClientConnection(new Socket(address, port));
    }

    @Override
    public void disConnect() throws IOException {
        close();
    }

}
