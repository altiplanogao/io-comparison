package being.altiplano.ioservice.mina;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.AbstractClient;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.LockSupport;

/**
 * Implementation of {@link being.altiplano.ioservice.IClient} using Mina
 */
public class MinaClient extends AbstractClient {
    private static final int CONNECT_TIMEOUT = 0_0;

    public class ClientSessionHandler extends IoHandlerAdapter {

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            super.messageSent(session, message);
        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            super.messageReceived(session, message);
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) {
            session.closeNow();
        }
    }

    private NioSocketConnector connector;
    private IoSession session;

    public MinaClient(String address, int port) {
        super(address, port);
    }

    @Override
    public void connect() throws IOException {
        connector = new NioSocketConnector();
        connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);

        connector.getFilterChain().addLast("msg", new ProtocolCodecFilter(new MsgCodecFactory()));
        connector.getFilterChain().addLast("logger", new LoggingFilter());

        connector.getSessionConfig().setUseReadOperation(true);
        connector.setHandler(new ClientSessionHandler());

        for (; ; ) {
            try {
                ConnectFuture future = connector.connect(new InetSocketAddress(address, port));
                future.awaitUninterruptibly();
                session = future.getSession();
                break;
            } catch (RuntimeIoException e) {
                System.err.println("Failed to connect.");
                e.printStackTrace();
                LockSupport.parkNanos(10_000);
            }
        }
    }

    @Override
    public void disconnect() throws IOException {
        session.closeNow().awaitUninterruptibly();
        connector.dispose(true);
    }

    @Override
    protected <T extends Reply> T writeAndRead(Command command) throws IOException {
        Msg cmd = command.toMsg();
        WriteFuture wf = session.write(cmd);
        wf.awaitUninterruptibly();

        ReadFuture readFuture = session.read();
        readFuture.awaitUninterruptibly();
        Msg msg = (Msg) readFuture.getMessage();

        Reply reply = MsgConverter.convertReply(msg);

        return (T) reply;
    }
}
