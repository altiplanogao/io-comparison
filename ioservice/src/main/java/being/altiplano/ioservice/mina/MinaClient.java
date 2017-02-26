package being.altiplano.ioservice.mina;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.AbstractClient;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

/**
 * Created by gaoyuan on 26/02/2017.
 */
public class MinaClient extends AbstractClient {

    private class ClientHandler extends IoHandlerAdapter {
        @Override
        public void messageReceived(IoSession iosession, Object message)
                throws Exception {
            IoBuffer bbuf = (IoBuffer) message;
            int code = bbuf.getInt();
            int len = bbuf.getInt();
            byte[] data = new byte[len];
            bbuf.get(data);
            Msg msg = new Msg(code, data);

            reply = MsgConverter.convertReply(msg);
            replyLatch.countDown();
        }
    }

    private IoConnector connector;
    private static IoSession session;

    public MinaClient(String address, int port) {
        super(address, port);
    }

    @Override
    public void connect() throws IOException {
        ClientHandler clientHandler = new ClientHandler();
        connector = new NioSocketConnector();
        connector.setHandler(clientHandler);
        ConnectFuture connFuture = connector.connect(new InetSocketAddress(address, port));
        connFuture.awaitUninterruptibly();
        session = connFuture.getSession();
        try {
            clientHandler.sessionOpened(session);
        } catch (Exception e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    @Override
    public void disConnect() throws IOException {
        connector.dispose(true);
    }

    private volatile CountDownLatch replyLatch;
    private volatile Reply reply;

    @Override
    protected <T extends Reply> T writeAndRead(Command command) throws IOException {
        CountDownLatch tempLatch = new CountDownLatch(1);
        replyLatch = tempLatch;

        Msg cmd = command.toMsg();
        IoBuffer buffer = IoBuffer.allocate(cmd.length + 8)
                .putInt(cmd.code)
                .putInt(cmd.length)
                .put(cmd.data).flip();
        session.write(buffer);

        try {
            replyLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return (T) reply;
    }
}
