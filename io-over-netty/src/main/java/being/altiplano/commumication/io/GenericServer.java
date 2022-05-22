package being.altiplano.commumication.io;

import being.altiplano.commumication.protocol.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;

class GenericServer extends Server {
    private final int magic;
    private final int frameSize;

    private volatile Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public GenericServer(int port, int magic, int frameSize) {
        super(port);
        this.magic = magic;
        this.frameSize = frameSize;
    }

    @Override
    public void start() throws InterruptedException {
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
                                    new ByteToFrameDecoder(frameSize), // inbound
                                    new FrameToBlockDecoder(magic), // inbound
                                    new BlockToByteEncoder(magic, frameSize) // outbound
                            );
                        }
                    });

            serverChannel = b.bind(this.port).sync().channel();
        } finally {
        }
    }

    @Override
    public void stop(boolean waitDone) throws InterruptedException {
        ChannelFuture closeFuture = serverChannel.close();
        Future<?> bsf = bossGroup.shutdownGracefully();
        Future<?> wsf = workerGroup.shutdownGracefully();
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
