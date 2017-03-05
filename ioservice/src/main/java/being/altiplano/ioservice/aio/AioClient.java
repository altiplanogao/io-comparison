package being.altiplano.ioservice.aio;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.AbstractClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Implementation of {@link being.altiplano.ioservice.IClient} using AIO
 */
public class AioClient extends AbstractClient {

    private class ClientMsgReader extends MsgReader {
        public ClientMsgReader(AsynchronousSocketChannel ch) {
            super(ch);
        }

        @Override
        protected void onMsg(Msg msg) {
            super.onMsg(msg);
            reply = MsgConverter.convertReply(msg);
            resultLatch.countDown();
        }
    }

    private class ClientMsgWriter extends MsgWriter {
        public ClientMsgWriter(AsynchronousSocketChannel ch) {
            super(ch);
        }

        @Override
        protected void onWriteDone() {
            super.onWriteDone();
            reader.read();
        }
    }

    private AsynchronousSocketChannel socketChannel;
    private ClientMsgWriter writer;
    private ClientMsgReader reader;
    private volatile CountDownLatch resultLatch;
    private volatile Reply reply;

    public AioClient(String address, int port) {
        super(address, port);
    }

    @Override
    public void connect() throws IOException, InterruptedException {
        close();
        socketChannel = AsynchronousSocketChannel.open();
        Future connect = socketChannel.connect(new InetSocketAddress(this.address, this.port));
        try {
            connect.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        writer = new ClientMsgWriter(socketChannel);
        reader = new ClientMsgReader(socketChannel);
    }

    @Override
    public void disconnect() throws IOException, InterruptedException {
        if (socketChannel != null) {
            socketChannel.close();
            socketChannel = null;
        }
    }

    @Override
    protected <T extends Reply> T writeAndRead(Command command) throws IOException {
        resultLatch = new CountDownLatch(1);
        writer.write(command.toMsg());
        try {
            resultLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (T) reply;
    }
}
