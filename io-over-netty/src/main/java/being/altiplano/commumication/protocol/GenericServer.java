package being.altiplano.commumication.protocol;

import java.util.function.Function;
import java.util.function.Supplier;

public class GenericServer<REQUEST, RESPONSE> extends Server<REQUEST, RESPONSE> {
    private final Server<byte[], byte[]> innerServer;

    private final Class<REQUEST> requestDataType;
    private final Function<byte[], REQUEST> requestDeserializer;

    private final Class<RESPONSE> responseDataType;
    private final Function<RESPONSE, byte[]> responseSerializer;

    public GenericServer(Supplier<Server<byte[], byte[]>> innerServerSupplier,
                         Class<REQUEST> requestDataType, Function<byte[], REQUEST> requestDeserializer,
                         Class<RESPONSE> responseDataType, Function<RESPONSE, byte[]> responseSerializer) {
        super();
        this.innerServer = innerServerSupplier.get();
        this.requestDataType = requestDataType;
        this.requestDeserializer = requestDeserializer;
        this.responseDataType = responseDataType;
        this.responseSerializer = responseSerializer;
    }

    @Override
    public void setProcessor(final Function<REQUEST, RESPONSE> processor) {
        super.setProcessor(processor);
        Function<byte[], byte[]> innerProcessor = requestBytes -> {
            REQUEST req = requestDeserializer.apply(requestBytes);
            RESPONSE res = processRequest(req);
            byte[] resBytes = responseSerializer.apply(res);
            return resBytes;
        };
        this.innerServer.setProcessor(innerProcessor);
    }

    public void start() throws InterruptedException {
        innerServer.start();
    }

    public void stop(boolean waitDone) throws InterruptedException {
        innerServer.stop(waitDone);
    }

    //
//    protected final void onReceiveRawRequest(ChannelHandlerContext ctx, byte[] rawRequest){
//        REQUEST request = requestDeserializer.apply(rawRequest);
//        requestListenable.fire(request);
//        //不太合理
//        RESPONSE response = requestHandler.apply(request);
//        responseListenable.fire(response);
//        ctx.write(response);
//    }
}
