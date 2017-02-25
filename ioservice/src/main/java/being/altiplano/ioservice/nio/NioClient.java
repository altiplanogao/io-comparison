package being.altiplano.ioservice.nio;

import being.altiplano.ioservice.AbstractClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Created by gaoyuan on 23/02/2017.
 */
public class NioClient extends AbstractClient {
    SocketChannel socketChannel;

    public NioClient(String address, int port) {
        super(address, port);
    }

    @Override
    public void connect() throws IOException {
        close();
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(this.address, this.port));
        clientConnection = new NioClientConnection(socketChannel);
    }

    @Override
    public void disConnect() throws IOException {
        close();
    }
}
