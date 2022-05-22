package being.altiplano.commumication.io;

import being.altiplano.commumication.protocol.Client;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

class GenericClient<REQUEST, RESPONSE> extends Client<REQUEST, RESPONSE> {
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final int magic;
    private final int frameSize;

    private EventLoopGroup workerGroup;
    private Channel channel;

    public GenericClient(String address, int port,
                         Class<REQUEST> requestDataType, Function<REQUEST, byte[]> requestSerializer,
                         Class<RESPONSE> responseDataType, Function<byte[], RESPONSE> requestDeserializer,
                         int magic, int frameSize) {
        super(address, port,
                requestDataType, requestSerializer,
                responseDataType, requestDeserializer);
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
    protected void rawRequest(byte[] bytesOfRequest) {
        channel.writeAndFlush(bytesOfRequest);
    }

    private void onReceiveRawResponse(ChannelHandlerContext ctx, byte[] rawResponse){
        RESPONSE response = responseDeserializer.apply(rawResponse);
    }


    private void onReceiveResponse(ChannelHandlerContext ctx, RESPONSE response){

    }

}
