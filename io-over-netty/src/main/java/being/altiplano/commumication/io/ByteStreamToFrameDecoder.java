package being.altiplano.commumication.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class ByteStreamToFrameDecoder extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteStreamToFrameDecoder.class);
    private enum State {
        HEADER,
        BODY
    }

    private String logPrefix = "";
    private final int magic;
    private final int frameSizeLimit;
    private State state = State.HEADER;
    private Frame frame;

    public ByteStreamToFrameDecoder(int magic) {
        this(magic, 128);
    }

    public ByteStreamToFrameDecoder(int magic, int frameSizeLimit) {
        this.magic = magic;
        if (frameSizeLimit < 8) {
            throw new IllegalArgumentException("Frame size limit too small");
        }
        this.frameSizeLimit = frameSizeLimit;
    }

    public ByteStreamToFrameDecoder setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
        return this;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        switch (state) {
            case HEADER:
                if (in.readableBytes() < Frame.HEAD_LEN) {
                    return;
                }
                int magic = in.readInt();
                if (magic != this.magic) {
                    throw new IllegalStateException(
                            String.format("magic code mismatch, expect: 0x%X, actual: 0x%X", this.magic, magic));
                }
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
                LOGGER.debug("{}: byte stream -> frame", logPrefix);
                frame = null;
                state = State.HEADER;
                break;
            default:
                throw new IllegalStateException("Unexpected");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}