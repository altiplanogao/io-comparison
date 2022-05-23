package being.altiplano.commumication.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class FrameToByteStreamEncoderTest {
    @Test
    public void testEncode(){
        EmbeddedChannel channel = new EmbeddedChannel(new FrameToByteStreamEncoder());

        Frame data0 = new Frame(0x12345678, (byte) 0b11011000, "Hello World".getBytes(StandardCharsets.UTF_8));
        channel.writeOutbound(data0);

        ByteBuf buf = channel.readOutbound();
        byte[] dataGot = new byte[buf.readableBytes()];
        buf.readBytes(dataGot);

        Assertions.assertArrayEquals(data0.getBytes(true, true), dataGot);
        Assertions.assertNull(channel.readOutbound());
    }
}