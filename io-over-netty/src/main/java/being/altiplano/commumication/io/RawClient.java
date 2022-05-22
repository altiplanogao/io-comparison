package being.altiplano.commumication.io;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

import java.util.concurrent.atomic.AtomicBoolean;

abstract class RawClient {
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

    public void start() throws InterruptedException {
        if (connected.compareAndSet(false, true)) {
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
                                    new ByteToFrameDecoder(frameSize), // inbound
                                    new FrameToBlockDecoder(magic), // inbound
                                    new BlockToRawObjectDecoder() {
                                        @Override
                                        protected void onReceiveData(ChannelHandlerContext ctx, byte[] rawResponse) {
                                            onReceiveRawResponse(ctx, rawResponse);
                                        }
                                    },
                                    new BlockToByteEncoder(magic, frameSize) // outbound
                            );
                        }
                    });

            ChannelFuture f = b.connect(address, port).sync();
            channel = f.channel();
        }
    }

    public void stop() throws InterruptedException {
        if (connected.compareAndSet(true, false)) {
            ChannelFuture cf = channel.close();
            Future<?> wgf = workerGroup.shutdownGracefully();
            cf.await();
            wgf.await();
        }
    }

    public void request(byte[] bytesOfRequest) {
        channel.writeAndFlush(bytesOfRequest);
    }

    protected abstract void onReceiveRawResponse(ChannelHandlerContext ctx, byte[] rawResponse);
}
