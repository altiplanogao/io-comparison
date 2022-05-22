package being.altiplano.commumication.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

class ByteToFrameDecoder extends ByteToMessageDecoder {
    private enum State {
        HEADER,
        BODY
    }

    private final int frameSizeLimit;
    private State state = State.HEADER;
    private Frame frame;

    public ByteToFrameDecoder() {
        this(128);
    }

    public ByteToFrameDecoder(int frameSizeLimit) {
        if (frameSizeLimit < 8) {
            throw new IllegalArgumentException("Frame size limit too small");
        }
        this.frameSizeLimit = frameSizeLimit;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        switch (state) {
            case HEADER:
                if (in.readableBytes() < Frame.HEAD_LEN) {
                    return;
                }
                int magic = in.readInt();
                int len = in.readInt();
                byte control = in.readByte();
                if (frame != null) {
                    throw new IllegalStateException("Decoder status unexpected");
                }
                if (len > frameSizeLimit) {
                    throw new IllegalStateException("Frame size too big");
                }
                frame = new Frame(magic, control, len);
                state = State.BODY;
                break;
            case BODY:
                if (in.readableBytes() < frame.getBodyLength()) {
                    return;
                }
                byte[] body = new byte[frame.getBodyLength()];
                in.readBytes(body, 0, frame.getBodyLength());
                frame.setBody(body, false);
                list.add(frame);
                frame = null;
                state = State.HEADER;
                break;
            default:
                throw new IllegalStateException("Unexpected");
        }
    }
}