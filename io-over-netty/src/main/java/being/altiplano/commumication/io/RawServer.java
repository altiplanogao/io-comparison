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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RawServer extends Server<byte[], byte[]> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RawServer.class);

    protected final int port;

    private final int magic;
    private final int frameSize;

    private volatile Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public RawServer(int port, int magic, int frameSize) {
        super();
        this.port = port;
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
                    .childHandler(createChannelInitializer());

            serverChannel = b.bind(this.port).sync().channel();
        } finally {
        }
    }

    private ChannelInitializer<SocketChannel> createChannelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline p = ch.pipeline();
                p.addLast(getChannelInitialHandlers());
            }
        };
    }

    private ChannelHandler[] getChannelInitialHandlers(){
        return new ChannelHandler[]{
                new ByteStreamToFrameDecoder(frameSize).setLogPrefix("server got request"), // inbound (request: bytes stream -> frame)
                new FramesToBlockDecoder(magic).setLogPrefix("server got request"), // inbound (request: frame -> block)
                new BlockToRawObjectDecoder(this::onReceiveRawRequest).setLogPrefix("server got request"), // inbound (request: block -> raw bytes)

                new FrameToByteStreamEncoder().setLogPrefix("server send response"),
                new SliceToFramesEncoder(magic,frameSize).setLogPrefix("server send response"),
//                        new SliceToByteStreamEncoder(magic, frameSize).setLogPrefix("server send response") // outbound (response: block -> frame -> bytes stream)
        };
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

    private void onReceiveRawRequest(ChannelHandlerContext ctx, byte[] rawRequest) {
        LOGGER.info("server: got request");
        byte[] response = super.processRequest(rawRequest);
        LOGGER.info("server: response prepared");
        if (response != null && response.length > 0) {
            LOGGER.info("server: write response");
            ctx.channel().writeAndFlush(new Slice(response));
            LOGGER.info("server: write response method returned");
        }
    }
}
