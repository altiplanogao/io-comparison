package being.altiplano.ioservice.aio;

import being.altiplano.ioservice.AbstractServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of {@link being.altiplano.ioservice.IServer} using AIO
 */
public class AioServer extends AbstractServer {
    private AsynchronousServerSocketChannel serverSocketChannel;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private CountDownLatch connectionLatch;

    public AioServer(int port) {
        super(port);
    }

    class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AioServer> {
        @Override
        public void completed(AsynchronousSocketChannel channel, AioServer serverHandler) {
            serverSocketChannel.accept(serverHandler, this);
            AioServerConnection connection = new AioServerConnection(channel);
            connection.processCommand();
        }

        @Override
        public void failed(Throwable exc, AioServer serverHandler) {
            if (exc instanceof AsynchronousCloseException) {
                connectionLatch.countDown();
            } else {
                exc.printStackTrace();
            }
        }
    }

    @Override
    public void start() throws IOException, InterruptedException {
        if (running.compareAndSet(false, true)) {
            final CountDownLatch latch = new CountDownLatch(1);

            final AsynchronousServerSocketChannel ssc = AsynchronousServerSocketChannel.open();
            ssc.bind(new InetSocketAddress(port));

            ssc.accept(this, new AcceptHandler());

            connectionLatch = latch;
            serverSocketChannel = ssc;
        }
    }

    @Override
    public void stop(boolean waitDone) throws IOException, InterruptedException {
        final CountDownLatch latch = connectionLatch;
        if (running.compareAndSet(true, false)) {
            try {
                if (serverSocketChannel != null) {
                    serverSocketChannel.close();
                    serverSocketChannel = null;
                }
            } catch (AsynchronousCloseException e) {
                e.printStackTrace();
            } finally {
                if (waitDone) {
                    latch.await();
                }
            }
        }
    }
}
