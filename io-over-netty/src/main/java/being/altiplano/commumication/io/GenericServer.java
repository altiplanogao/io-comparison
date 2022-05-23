package being.altiplano.commumication.io;

import being.altiplano.commumication.protocol.Server;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.Function;

public class GenericServer<REQUEST, RESPONSE> extends Server<REQUEST, RESPONSE> {
    private final RawServer rawServer;

    public GenericServer(int port,
                         Class<REQUEST> requestDataType, Function<byte[], REQUEST> requestDeserializer,
                         Class<RESPONSE> responseDataType, Function<RESPONSE, byte[]> responseSerializer,
                         Function<REQUEST, RESPONSE> requestHandler,
                         int magic, int frameSize) {
        super(requestDataType, requestDeserializer,
                responseDataType, responseSerializer,
                requestHandler);
        this.rawServer = new RawServer(port, magic, frameSize) {
            @Override
            protected void onReceiveRawRequest(ChannelHandlerContext ctx, byte[] rawRequest) {
                GenericServer.this.handleRawRequest(ctx, rawRequest);
            }
        };
    }

    @Override
    public void start() throws InterruptedException {
        rawServer.start();
    }

    @Override
    public void stop(boolean waitDone) throws InterruptedException {
        rawServer.stop(waitDone);
    }

    private void handleRawRequest(ChannelHandlerContext ctx, byte[] rawRequest) {
        super.onReceiveRawRequest(ctx, rawRequest);
    }
}
