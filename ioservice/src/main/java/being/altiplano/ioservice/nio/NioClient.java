package being.altiplano.ioservice.nio;

import being.altiplano.ioservice.ConnectionClient;
import being.altiplano.ioservice.IClientAccess;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Implementation of {@link being.altiplano.ioservice.IClient} using NIO
 */
public class NioClient extends ConnectionClient {
    private SocketChannel socketChannel;

    public NioClient(String address, int port) {
        super(address, port);
    }

    @Override
    protected void doConnect() throws IOException, InterruptedException {
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(this.address, this.port));
    }

    @Override
    protected void doDisConnect() throws IOException, InterruptedException {
        if (socketChannel != null) {
            socketChannel.close();
        }
    }

    @Override
    protected IClientAccess createAccess() throws IOException {
        return new NioClientAccess(socketChannel);
    }
}
