package being.altiplano.commumication.io;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class FramesToBlockDecoder extends MessageToMessageDecoder<Frame> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FramesToBlockDecoder.class);
    private final int magic;
    private final List<Frame> pending = new ArrayList<>();
    private String logPrefix = "";

    FramesToBlockDecoder(int magic) {
        this.magic = magic;
    }

    public FramesToBlockDecoder setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
        return this;
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
            LOGGER.info("{}: {} frame(s) -> block", logPrefix, pending.size());
            list.add(block);
            pending.clear();
        } else {
            pending.add(frame);
        }
    }
}
