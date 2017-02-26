package being.altiplano.ioservice.mina;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.AbstractServer;
import being.altiplano.ioservice.ServerCommandHandler;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by gaoyuan on 26/02/2017.
 */
public class MinaServer extends AbstractServer {
    private IoAcceptor acceptor;

    public static class ServerHandler extends IoHandlerAdapter {
        private ServerCommandHandler commandHandler = new ServerCommandHandler();

        public void exceptionCaught(IoSession session, Throwable cause)
                throws Exception {
            cause.printStackTrace();
        }

        public void messageReceived(IoSession session, Object message)
                throws Exception {
            IoBuffer buffer = (IoBuffer) message;
            int code = buffer.getInt();
            int len = buffer.getInt();
            byte[] data = new byte[len];
            buffer.get(data);
            Msg msg = new Msg(code, data);
            Command command = MsgConverter.convert(msg);
            Reply reply = commandHandler.handle(command);
            Msg replyMsg = reply.toMsg();
            int totalLen = replyMsg.length + 8;
            IoBuffer bb = IoBuffer.allocate(totalLen)
                    .putInt(replyMsg.code)
                    .putInt(replyMsg.length)
                    .put(replyMsg.data)
                    .flip();
            session.write(bb);
        }
    }

    @Override
    public void start() throws IOException {
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.setHandler(new ServerHandler());

        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

        acceptor.setCloseOnDeactivation(true);
        acceptor.bind(new InetSocketAddress(port));
    }

    @Override
    public void stop(boolean waitDone) throws IOException, InterruptedException {
        close();
    }

    @Override
    public void close() throws IOException {
        acceptor.unbind();
        acceptor = null;
    }

    public MinaServer(int port) {
        super(port);
    }
}
