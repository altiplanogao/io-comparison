package being.altiplano.commumication.io;

import being.altiplano.commumication.protocol.Client;
import being.altiplano.commumication.protocol.Listenable;
import being.altiplano.commumication.protocol.Listener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

import java.util.concurrent.atomic.AtomicBoolean;

class RawClient implements Client<byte[], byte[]> {
    private final AtomicBoolean connected = new AtomicBoolean(false);

    protected final String address;
    protected final int port;
    private final int magic;
    private final int frameSize;

    private EventLoopGroup workerGroup;
    private Channel channel;

    private final Listenable<byte[]> responseListenable = new Listenable<>();

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
                    .handler(createChannelInitializer());

            ChannelFuture f = b.connect(address, port).sync();
            channel = f.channel();
        }
    }

    private ChannelInitializer<SocketChannel> createChannelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                p.addLast(
                        //new LoggingHandler(LogLevel.INFO),
                        new ByteToFrameDecoder(frameSize), // inbound (response: bytes stream -> frame)
                        new FrameToBlockDecoder(magic), // inbound (response: frame -> block)
                        new BlockToRawObjectDecoder() {
                            @Override
                            protected void onReceiveData(ChannelHandlerContext ctx, byte[] rawResponse) {
                                onReceiveRawResponse(ctx, rawResponse);
                            }
                        }, // inbound (response: block -> raw response bytes)
                        new BlockToByteEncoder(magic, frameSize) // outbound (request: block -> frame -> bytes stream)
                );
            }
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
        channel.writeAndFlush(bytesOfRequest);
    }

    @Override
    public void registerResponseListener(Listener<byte[]> listener) {
        responseListenable.addListener(listener);
    }

    protected final void onReceiveRawResponse(ChannelHandlerContext ctx, byte[] rawResponse) {
        responseListenable.fire(rawResponse);
    }
}
