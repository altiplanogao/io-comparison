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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by gaoyuan on //.
 */
public class MinaClient extends AbstractClient {
    private static final int CONNECT_TIMEOUT = 0_0;

    private class ClientHandler extends IoHandlerAdapter {
        @Override
        public void messageReceived(IoSession iosession, Object message)
                throws Exception {
            super.messageReceived(iosession, message);
        }

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            super.messageSent(session, message);
        }
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientSessionHandler.class);

    public class ClientSessionHandler extends IoHandlerAdapter {
        //    private final int[] values;
        private boolean finished;

        public ClientSessionHandler() {
            //                 this.values = values;
        }

        public boolean isFinished() {
            return finished;
        }

        @Override
        public void sessionOpened(IoSession session) {
            // send summation requests
//                      for (int i = 0; i < values.length; i++) {
//                              AddMessage m = new AddMessage();
//                              m.setSequence(i);
//                              m.setValue(values[i]);
//                              session.write(m);
//                          }
        }

        @Override
        public void messageReceived(IoSession session, Object message) {
            // server only sends ResultMessage. otherwise, we will have to identify
            // its type using instanceof operator.
            Msg rm = (Msg) message;
//                      if (rm.isOk()) {
//                              // server returned OK code.
//                              // if received the result message which has the last sequence
//                              // number,
//                              // it is time to disconnect.
//                              if (rm.getSequence() == values.length - 1) {
//                                      // print the sum and disconnect.
//                                      LOGGER.info("The sum: " + rm.getValue());
//                                      session.closeNow();
//                                      finished = true;
//                                  }
//                          } else {
//                              // seever returned error code because of overflow, etc.
//                              LOGGER.warn("Server error, disconnecting...");
//                              session.closeNow();
//                              finished = true;
//                          }
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
        //       ClientHandler clientHandler = new ClientHandler();
        connector = new NioSocketConnector();
        connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);

        connector.getFilterChain().addLast("msg", new ProtocolCodecFilter(new MsgCodecFactory()));
        connector.getFilterChain().addLast("logger", new LoggingFilter());

        connector.getSessionConfig().setUseReadOperation(true);
        connector.setHandler(new ClientSessionHandler());

        ConnectFuture connFuture = connector.connect(new InetSocketAddress(address, port));
        connFuture.awaitUninterruptibly();
        session = connFuture.getSession();

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

//        try {
//            clientHandler.sessionOpened(session);
//        } catch (Exception e) {
//            throw new IOException(e);
//        }
    }

    @Override
    public void disConnect() throws IOException {
        session.closeNow().awaitUninterruptibly();
        connector.dispose(true);
//        connector.dispose(true);
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
