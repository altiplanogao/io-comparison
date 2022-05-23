package being.altiplano.commumication.protocol;

import io.netty.channel.ChannelHandlerContext;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

public abstract class Server<REQUEST, RESPONSE> implements Closeable {

    private final Class<REQUEST> requestDataType;
    private final Function<byte[],REQUEST> requestDeserializer;

    private final Class<RESPONSE> responseDataType;
    private final Function< RESPONSE, byte[]> responseSerializer;

    private final Listenable<REQUEST> requestListenable = new Listenable<>();

    private final Listenable<RESPONSE> responseListenable = new Listenable<>();


    private final Function<REQUEST, RESPONSE> requestHandler;

    public Server(Class<REQUEST> requestDataType, Function<byte[], REQUEST> requestDeserializer,
                  Class<RESPONSE> responseDataType, Function<RESPONSE, byte[]> responseSerializer,
                  Function<REQUEST, RESPONSE> requestHandler) {
        this.requestDataType = requestDataType;
        this.requestDeserializer = requestDeserializer;
        this.responseDataType = responseDataType;
        this.responseSerializer = responseSerializer;
        this.requestHandler = requestHandler;
    }

    public abstract void start() throws InterruptedException;

    public abstract void stop(boolean waitDone) throws InterruptedException;

    public final void close() throws IOException {
        try {
            stop(true);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    protected final void onReceiveRawRequest(ChannelHandlerContext ctx, byte[] rawRequest){
        REQUEST request = requestDeserializer.apply(rawRequest);
        requestListenable.fire(request);
        //不太合理
        RESPONSE response = requestHandler.apply(request);
        responseListenable.fire(response);
        ctx.write(response);
    }
}
