package being.altiplano.commumication.io;

import being.altiplano.commumication.protocol.Client;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

class RawClient extends Client<byte[], byte[]> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RawClient.class);

    private final AtomicBoolean connected = new AtomicBoolean(false);

    protected final String address;
    protected final int port;
    private final int magic;
    private final int frameSize;

    private EventLoopGroup workerGroup;
    private Channel channel;

    public RawClient(String address, int port,
                     int magic, int frameSize) {
        this.address = address;
        this.port = port;
        this.magic = magic;
        this.frameSize = frameSize;
    }

    @Override
    public void start() throws InterruptedException {
        if (connected.compareAndSet(false, true)) {
            workerGroup = new NioEventLoopGroup();

            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(getChannelInitialHandlers());
                        }
                    });

            ChannelFuture f = b.connect(address, port).sync();
            channel = f.channel();
        }
    }

    private ChannelHandler[] getChannelInitialHandlers() {
        return new ChannelHandler[]{
//                new LoggingHandler(LogLevel.TRACE),
                new ByteStreamToFrameDecoder(magic, frameSize).setLogPrefix("client got response"), // inbound (response: bytes stream -> frame)
                new FramesToBlockDecoder(magic).setLogPrefix("client got response"), // inbound (response: frame -> block)
                new BlockToRawObjectDecoder(this::onReceiveResponse).setLogPrefix("client got response"), // inbound (response: block -> raw bytes)

                new FrameToByteStreamEncoder().setLogPrefix("client send request"),
                new SliceToFramesEncoder(magic, frameSize).setLogPrefix("client send request"),
//                new SliceToByteStreamEncoder(magic, frameSize).setLogPrefix("client send request") // outbound (request: block -> frame -> bytes stream)
        };
    }

    @Override
    public void stop() throws InterruptedException {
        if (connected.compareAndSet(true, false)) {
            ChannelFuture cf = channel.close();
            Future<?> wgf = workerGroup.shutdownGracefully();
            cf.await();
            wgf.await();
        }
    }

    @Override
    public void request(byte[] bytesOfRequest) {
        LOGGER.debug("make request");
        channel.writeAndFlush(new Slice(bytesOfRequest));
    }

    protected final void onReceiveResponse(ChannelHandlerContext ctx, byte[] response) {
        super.fireResponseReceivedEvent(response);
    }
}
