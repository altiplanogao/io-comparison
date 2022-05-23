package being.altiplano.commumication.io;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class ByteStreamToFrameDecoderTest {
    @Test
    public void testDecodeOnce() {
        EmbeddedChannel channel = new EmbeddedChannel(new ByteStreamToFrameDecoder());

        Frame data = new Frame(0x12345678, (byte) 0b11011000, "Hello World".getBytes(StandardCharsets.UTF_8));

        Assertions.assertFalse(channel.writeInbound(Unpooled.buffer().writeBytes(data.headBytes())));
        Slice body = data.getBody();
        Assertions.assertTrue(channel.writeInbound(Unpooled.buffer().writeBytes(body.data, body.offset, body.length)));

        Assertions.assertTrue(channel.finish());
        Frame dataGot = channel.readInbound();

        Assertions.assertEquals(data, dataGot);
        Assertions.assertNull(channel.readInbound());
    }

    @Test
    public void testDecodeTwice() {
        EmbeddedChannel channel = new EmbeddedChannel(new ByteStreamToFrameDecoder());

        Frame data0 = new Frame(0x12345678, (byte) 0b11011000, "Hello World".getBytes(StandardCharsets.UTF_8));
        Frame data1 = new Frame(0x87654321, (byte) 0b00100111, "Hello World2".getBytes(StandardCharsets.UTF_8));
        for (Frame data : new Frame[]{data0, data1}) {
            Slice body = data.getBody();
            channel.writeInbound(Unpooled.buffer().writeBytes(data.headBytes()));
            channel.writeInbound(Unpooled.buffer().writeBytes(body.data, body.offset, body.length));
        }

        Assertions.assertTrue(channel.finish()); //5
        Frame dataGot0 = channel.readInbound();
        Assertions.assertEquals(data0, dataGot0);

        Frame dataGot1 = channel.readInbound();
        Assertions.assertEquals(data1, dataGot1);

        Frame dataGot2 = channel.readInbound();
        Assertions.assertNull(dataGot2);
    }

    @Test
    public void testDecodeFrameTooBig() {
        Frame data = new Frame(0x12345678, (byte) 0b11011000, "Hello World! Hello World! ".getBytes(StandardCharsets.UTF_8));

        EmbeddedChannel channel = new EmbeddedChannel(new ByteStreamToFrameDecoder(10));

        Assertions.assertThrows(DecoderException.class, () ->
                Assertions.assertFalse(channel.writeInbound(Unpooled.buffer().writeBytes(data.headBytes()))));
    }
}