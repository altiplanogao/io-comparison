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
 * Created by gaoyuan on 23/02/2017.
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
    public void start() throws IOException {
        if (running.compareAndSet(false, true)) {
            final CountDownLatch latch = new CountDownLatch(1);
            selector = Selector.open();

            final ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(port));

            SelectionKey key = ssc.register(selector, SelectionKey.OP_ACCEPT);

            Runnable selectRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        while (running.get()) {
                            try {
                                int num = selector.select();

                                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                                while (keyIterator.hasNext()) {
                                    try {
                                        SelectionKey key = keyIterator.next();
                                        Object attachment = key.attachment();
                                        if (key.isAcceptable()) {
                                            // a connection was accepted by a ServerSocketChannel.
                                            SocketChannel socketChannel = ssc.accept();
                                            socketChannel.configureBlocking(false);
                                            NioServerConnection connection = new NioServerConnection(socketChannel);
                                            socketChannel.register(selector,
                                                    SelectionKey.OP_CONNECT | SelectionKey.OP_READ,
                                                    //  | SelectionKey.OP_WRITE,
                                                    connection);
                                        } else if (key.isConnectable()) {
                                            NioServerConnection connection = (NioServerConnection) attachment;
                                        } else if (key.isReadable()) {
                                            NioServerConnection connection = (NioServerConnection) attachment;
                                            Command command = connection.readCommand();
                                            Reply reply = processCommand(connection, command);
                                            if (reply instanceof StopReply) {
                                                key.channel().close();
                                            }
                                        } else if (key.isWritable()) {
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
                }
            };
            connectionLatch = latch;
            serverSocketChannel = ssc;
            new Thread(selectRunnable).start();
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

    @Override
    public void close() throws IOException {
        try {
            stop(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
