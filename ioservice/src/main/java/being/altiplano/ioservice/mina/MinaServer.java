package being.altiplano.ioservice.mina;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.AbstractServer;
import being.altiplano.ioservice.ServerCommandHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.LockSupport;

/**
 * Implementation of {@link being.altiplano.ioservice.IServer} using Mina
 */
public class MinaServer extends AbstractServer {
    private static final long START_TIMEOUT = 1000;
    private NioSocketAcceptor acceptor;

    public static class ServerHandler extends IoHandlerAdapter {
        private ServerCommandHandler commandHandler = new ServerCommandHandler();

        public void exceptionCaught(IoSession session, Throwable cause)
                throws Exception {
            cause.printStackTrace();
        }

        public void messageReceived(IoSession session, Object message)
                throws Exception {
            Command command = MsgConverter.convert((Msg) message);
            Reply reply = commandHandler.handle(command);
            Msg replyMsg = reply.toMsg();
            session.write(replyMsg);
        }
    }

    public MinaServer(int port) {
        super(port);
    }

    @Override
    public void start() throws IOException, InterruptedException {
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("msg", new ProtocolCodecFilter(new MsgCodecFactory()));
        acceptor.setHandler(new ServerHandler());

        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

        acceptor.setCloseOnDeactivation(true);
        acceptor.setReuseAddress(true);

        long start = System.currentTimeMillis();
        do {
            boolean timeout = (System.currentTimeMillis() - start > START_TIMEOUT);
            try {
                acceptor.bind(new InetSocketAddress(port));
                break;
            } catch (IOException e) {
                if (timeout) {
                    throw e;
                }
                System.out.println("spinning");
                LockSupport.parkNanos(100_000_000);
            }
        } while (true);
    }

    @Override
    public void stop(boolean waitDone) throws IOException, InterruptedException {
        if (acceptor.isActive()) {
            for (IoSession ss : acceptor.getManagedSessions().values()) {
                ss.closeNow().await();
            }

        }
        acceptor.dispose(waitDone);
        acceptor.unbind();
        acceptor = null;
    }
}
