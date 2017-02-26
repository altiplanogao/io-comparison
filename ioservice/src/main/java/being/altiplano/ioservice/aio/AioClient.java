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
 * Created by gaoyuan on 23/02/2017.
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
    public void connect() throws IOException {
        close();
        socketChannel = AsynchronousSocketChannel.open();
        Future connect = socketChannel.connect(new InetSocketAddress(this.address, this.port));
        try {
            connect.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        clientConnection = null;
        writer = new ClientMsgWriter(socketChannel);
        reader = new ClientMsgReader(socketChannel);
    }

    @Override
    public void disConnect() throws IOException {
        close();
    }

    @Override
    public void close() throws IOException {
        super.close();
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
