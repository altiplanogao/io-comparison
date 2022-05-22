package being.altiplano.commumication.io;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

abstract class BlockToRawObjectDecoder extends MessageToMessageDecoder<Block> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Block block, List<Object> list) throws Exception {
        byte[] rawRequest = block.data;
        onReceiveData(channelHandlerContext, rawRequest);
        list.add(rawRequest);
    }

    protected abstract void onReceiveData(ChannelHandlerContext ctx, byte[] rawRequest);
}
