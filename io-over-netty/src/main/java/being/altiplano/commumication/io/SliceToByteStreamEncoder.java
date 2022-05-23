package being.altiplano.commumication.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class SliceToByteStreamEncoder extends MessageToByteEncoder<Slice> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SliceToByteStreamEncoder.class);

    private final int magic;
    private final int frameSize;
    private String logPrefix = "";

    public SliceToByteStreamEncoder(int magic, int frameSize) {
        this.magic = magic;
        if (frameSize <= 0) {
            throw new IllegalArgumentException();
        }
        this.frameSize = frameSize;
    }

    public SliceToByteStreamEncoder setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
        return this;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Slice block, ByteBuf out) throws Exception {
        List<Frame> frames = SliceToFramesEncoder.makeFrames(block, magic, frameSize);
        if (!frames.isEmpty()){
            LOGGER.info("{}: block -> {} frame(s)", logPrefix, frames.size());
        }
        frames.forEach(f -> writeFrame(f, out));
    }

    private void writeFrame(Frame frame, ByteBuf out) {
        out.writeBytes(frame.headBytes());
        Slice body = frame.getBody();
        out.writeBytes(body.data, body.offset, body.length);
        LOGGER.info("{}: frame -> bytes stream", logPrefix);
    }
}
