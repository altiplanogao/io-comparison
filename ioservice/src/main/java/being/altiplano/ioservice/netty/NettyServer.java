package being.altiplano.ioservice.netty;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.AbstractServer;
import being.altiplano.ioservice.ServerCommandHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;

import java.io.IOException;

/**
 * Implementation of {@link being.altiplano.ioservice.IServer} using Mina
 */
public class NettyServer extends AbstractServer {
    private volatile Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    static class ServerHandler extends SimpleChannelInboundHandler<Msg> {
        private final ServerCommandHandler handler = new ServerCommandHandler();

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {
            Command command = MsgConverter.convert(msg);
            Reply reply = handler.handle(command);
            ctx.writeAndFlush(reply.toMsg());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }

    public NettyServer(int port) {
        super(port);
    }

    @Override
    public void start() throws IOException, InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 0)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(
//new LoggingHandler(LogLevel.INFO)
                                    new MsgEncoder(),
                                    new MsgDecoder(),
                                    new ServerHandler()
                            );
                        }
                    });

            serverChannel = b.bind(this.port).sync().channel();
        } finally {

        }
    }

    @Override
    public void stop(boolean waitDone) throws IOException, InterruptedException {
        ChannelFuture closeFuture = serverChannel.close();
        Future bsf = bossGroup.shutdownGracefully();
        Future wsf = workerGroup.shutdownGracefully();
        if (waitDone) {
            try {
                closeFuture.await();
                bsf.await();
                wsf.await();
            } finally {

            }
        }
    }
}