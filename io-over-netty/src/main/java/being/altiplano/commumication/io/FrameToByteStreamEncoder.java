package being.altiplano.commumication.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameToByteStreamEncoder extends MessageToByteEncoder<Frame> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameToByteStreamEncoder.class);

    private String logPrefix = "";


    public FrameToByteStreamEncoder setLogPrefix(String logPrefix) {
        this.logPrefix = logPrefix;
        return this;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Frame frame, ByteBuf out) throws Exception {
        out.writeBytes(frame.headBytes());
        Slice body = frame.getBody();
        out.writeBytes(body.data, body.offset, body.length);
        LOGGER.debug("{}: frame -> bytes stream", logPrefix);
    }
}
