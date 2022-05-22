package being.altiplano.ioservice.netty;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.AbstractClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of {@link being.altiplano.ioservice.IClient} using Netty
 */
public class NettyClient extends AbstractClient {

    private EventLoopGroup workerGroup;
    private Channel channel;

    private volatile CountDownLatch replyLatch;
    private volatile Reply reply;

    class NettyClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            reply = MsgConverter.convertReply((Msg) msg);
            replyLatch.countDown();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }

    public NettyClient(String address, int port) {
        super(address, port);
    }

    @Override
    public void connect() throws IOException, InterruptedException {
        workerGroup = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(
                                //new LoggingHandler(LogLevel.INFO),
                                new MsgEncoder(),
                                new MsgDecoder(),
                                new NettyClientHandler()
                        );
                    }
                });

        ChannelFuture f = b.connect(address, port).sync();
        channel = f.channel();
    }

    @Override
    public void disconnect() throws IOException, InterruptedException {
        ChannelFuture cf = channel.close();
        Future wgf = workerGroup.shutdownGracefully();
        cf.await();
        wgf.await();
    }

    @Override
    protected <T extends Reply> T writeAndRead(Command command) throws IOException {
        replyLatch = new CountDownLatch(1);
        try {
            ChannelFuture cf = channel.write(command.toMsg());
            channel.flush();
            cf.awaitUninterruptibly();
            cf.get();
            channel.read();
            replyLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return (T) reply;
    }
}