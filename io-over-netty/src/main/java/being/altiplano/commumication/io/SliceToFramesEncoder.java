package being.altiplano.commumication.io;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SliceToFramesEncoder extends MessageToMessageEncoder<Slice> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SliceToFramesEncoder.class);
    private final int magic;
    private final int frameSize;

    private String logPrefix = "";

    public SliceToFramesEncoder setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
        return this;
    }

    public SliceToFramesEncoder(int magic, int frameSize) {
        this.magic = magic;
        if (frameSize <= 0) {
            throw new IllegalArgumentException();
        }
        this.frameSize = frameSize;
    }

    @Override
    protected void encode(ChannelHandlerContext context, Slice block, List<Object> list) throws Exception {
        List<Frame> frames = makeFrames(block, magic, frameSize);
        if (!frames.isEmpty()) {
            LOGGER.info("{}: block -> {} frame(s)", logPrefix, frames.size());
            list.addAll(frames);
        }
    }

    public static List<Frame> makeFrames(Slice block, int magic, int frameSize) {
        List<Frame> result = new ArrayList<>();
        int frameIndex = 0;
        final int first = block.offset;
        final int last = block.offset + block.length;
        int offset = first;
        while (offset < last) {
            int till = Math.min(offset + frameSize, last);
            Slice slice = new Slice(block.data, offset, till - offset);
            byte control = Frame.IS_MID_MASK;
            if (frameIndex == 0) {
                control |= Frame.IS_HEAD_MASK;
            }
            if (till == last) {
                control |= Frame.IS_TAIL_MASK;
            }
            Frame frame = new Frame(magic, control, slice);
            result.add(frame);
            offset = till;
            frameIndex++;
        }

        return result;
    }

}
