package being.altiplano.ioservice.nio;

import being.altiplano.config.Command;
import being.altiplano.config.Reply;
import being.altiplano.config.replies.StopReply;
import being.altiplano.ioservice.AbstractServer;
import being.altiplano.ioservice.ServerCommandHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of {@link being.altiplano.ioservice.IServer} using NIO
 */
public class NioServer extends AbstractServer {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ServerCommandHandler commandHandler = new ServerCommandHandler();
    private CountDownLatch connectionLatch;

    public NioServer(int port) {
        super(port);
    }

    @Override
    public void start() throws IOException, InterruptedException {
        if (running.compareAndSet(false, true)) {
            final ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(port));

            selector = Selector.open();
            SelectionKey key = ssc.register(selector, SelectionKey.OP_ACCEPT);

            final CountDownLatch latch = new CountDownLatch(1);
            Runnable selectRunnable = () -> {
                try {
                    while (running.get()) {
                        try {
                            int num = selector.select();

                            Set<SelectionKey> selectedKeys = selector.selectedKeys();
                            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                            while (keyIterator.hasNext()) {
                                try {
                                    SelectionKey key1 = keyIterator.next();
                                    Object attachment = key1.attachment();
                                    if (key1.isAcceptable()) {
                                        // The connection was accepted by a ServerSocketChannel.
                                        SocketChannel socketChannel = ssc.accept();
                                        socketChannel.configureBlocking(false);
                                        NioServerConnection connection = new NioServerConnection(socketChannel);
                                        socketChannel.register(selector,
                                                SelectionKey.OP_CONNECT | SelectionKey.OP_READ,
                                                //  | SelectionKey.OP_WRITE,
                                                connection);
                                    } else if (key1.isConnectable()) {
                                        // The connection was established with a remote server.
                                        NioServerConnection connection = (NioServerConnection) attachment;
                                    } else if (key1.isReadable()) {
                                        // The channel is ready for reading
                                        NioServerConnection connection = (NioServerConnection) attachment;
                                        Command command = connection.readCommand();
                                        Reply reply = processCommand(connection, command);
                                        if (reply instanceof StopReply) {
                                            key1.channel().close();
                                        }
                                    } else if (key1.isWritable()) {
                                        //  The channel is ready for writing
                                        NioServerConnection connection = (NioServerConnection) attachment;
                                    }
                                } finally {
                                    keyIterator.remove();
                                }
                            }
                        } catch (IOException e) {
                            if (running.get()) {
                                e.printStackTrace();
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            };
            connectionLatch = latch;
            serverSocketChannel = ssc;
            //es.submit(selectRunnable);
            (new Thread(selectRunnable)).start();
        }
    }

    private Reply processCommand(NioServerConnection connection, Command command) throws IOException {
        Reply reply = commandHandler.handle(command);
        connection.writeReply(reply);
        return reply;
    }

    @Override
    public void stop(boolean waitDone) throws IOException, InterruptedException {
        final CountDownLatch latch = connectionLatch;
        if (running.compareAndSet(true, false)) {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
                serverSocketChannel = null;
                selector.wakeup();
            }
            if (waitDone) {
                latch.await();
            }
        }
    }
}
