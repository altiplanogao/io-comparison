package being.altiplano.commumication.io;

import being.altiplano.commumication.protocol.Client;
import io.netty.channel.*;

import java.util.function.Function;

class GenericClient<REQUEST, RESPONSE> extends Client<REQUEST, RESPONSE> {
    private final RawClient rawClient;

    public GenericClient(String address, int port,
                         Class<REQUEST> requestDataType, Function<REQUEST, byte[]> requestSerializer,
                         Class<RESPONSE> responseDataType, Function<byte[], RESPONSE> requestDeserializer,
                         int magic, int frameSize) {
        super(requestDataType, requestSerializer,
                responseDataType, requestDeserializer);
        rawClient = new RawClient(address, port, magic, frameSize) {
            @Override
            protected void onReceiveRawResponse(ChannelHandlerContext ctx, byte[] rawResponse) {
                GenericClient.this.onReceiveRawResponse(rawResponse);
            }
        };
    }

    @Override
    public void start() throws InterruptedException {
        rawClient.start();
    }

    @Override
    public void stop() throws InterruptedException {
        rawClient.stop();
    }

    @Override
    protected void rawRequest(byte[] bytesOfRequest) {
        rawClient.request(bytesOfRequest);
    }
}
