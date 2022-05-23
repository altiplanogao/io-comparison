package being.altiplano.commumication.io;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class BlockToRawObjectDecoder extends MessageToMessageDecoder<Block> {
    public interface ReceiveDataHandler {
        void onData(ChannelHandlerContext ctx, byte[] data);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockToRawObjectDecoder.class);
    private String logPrefix = "";

    public BlockToRawObjectDecoder(ReceiveDataHandler receiveDataHandler) {
        this.receiveDataHandler = receiveDataHandler;
    }

    private final ReceiveDataHandler receiveDataHandler;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Block block, List<Object> list) throws Exception {
        byte[] rawRequest = block.data;
        LOGGER.info("{}: block -> raw object bytes", logPrefix);
        if (receiveDataHandler != null) {
            receiveDataHandler.onData(channelHandlerContext, rawRequest);
        }
        list.add(rawRequest);
    }

    public BlockToRawObjectDecoder setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
        return this;
    }
}
