package being.altiplano.commumication.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.ArrayList;
import java.util.List;

class BlockToByteEncoder extends MessageToByteEncoder<ByteSlice> {
    private final int magic;
    private final int frameSize;

    public BlockToByteEncoder(int magic, int frameSize) {
        this.magic = magic;
        if (frameSize <= 0) {
            throw new IllegalArgumentException();
        }
        this.frameSize = frameSize;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteSlice block, ByteBuf out) throws Exception {
        List<Frame> frames = makeFrames(block);
        frames.forEach(f -> writeFrame(f, out));
    }

    private List<Frame> makeFrames(ByteSlice block) {
        List<Frame> result = new ArrayList<>();
        int frameIndex = 0;
        final int first = block.offset;
        final int last = block.offset + block.length;
        int offset = first;
        while (offset < last) {
            int till = Math.min(offset + frameSize, last);
            ByteSlice slice = new ByteSlice(block.data, offset, till - offset);
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

    private void writeFrame(Frame frame, ByteBuf out) {
        out.writeBytes(frame.headBytes());
        ByteSlice body = frame.getBody();
        out.writeBytes(body.data, body.offset, body.length);
    }
}
