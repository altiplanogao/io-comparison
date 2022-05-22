package being.altiplano.commumication.io;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.ArrayList;
import java.util.List;

class FrameToBlockDecoder extends MessageToMessageDecoder<Frame> {
    private final int magic;
    private final List<Frame> pending = new ArrayList<>();

    FrameToBlockDecoder(int magic) {
        this.magic = magic;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Frame frame, List<Object> list) throws Exception {
        if (frame.getMagic() != magic) {
            return;
        }
        if (frame.containsTail()) {
            pending.add(frame);
            Frame merged = Frame.makeIntegral(pending.toArray(new Frame[0]));
            Block block = new Block(merged.getBody().getUsedBytes());
            list.add(block);
            pending.clear();
        } else {
            pending.add(frame);
        }
    }
}
