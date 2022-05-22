package being.altiplano.commumication.io;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class BlockToByteEncoderTest {
    @Test
    public void testEncodeWithOneFrame() {
        givenAndExpects(0x12345678, 10, "0123456789",
                new Frame(0x12345678, (byte) 0b11000000, "0123456789".getBytes(StandardCharsets.UTF_8)));
        givenAndExpects(0x12345678, 10, "012345678",
                new Frame(0x12345678, (byte) 0b11000000, "012345678".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testEncodeWith2Frames() {
        givenAndExpects(0x12345678, 10, "01234567891",
                new Frame(0x12345678, (byte) 0b10000000, "0123456789".getBytes(StandardCharsets.UTF_8)),
                new Frame(0x12345678, (byte) 0b01000000, "1".getBytes(StandardCharsets.UTF_8)));
        givenAndExpects(0x12345678, 10, "0123456789112345678",
                new Frame(0x12345678, (byte) 0b10000000, "0123456789".getBytes(StandardCharsets.UTF_8)),
                new Frame(0x12345678, (byte) 0b01000000, "112345678".getBytes(StandardCharsets.UTF_8)));
        givenAndExpects(0x12345678, 10, "01234567891123456789",
                new Frame(0x12345678, (byte) 0b10000000, "0123456789".getBytes(StandardCharsets.UTF_8)),
                new Frame(0x12345678, (byte) 0b01000000, "1123456789".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testEncodeWith3Frames() {
        givenAndExpects(0x12345678, 10, "01234567891123456789212345678",
                new Frame(0x12345678, (byte) 0b10000000, "0123456789".getBytes(StandardCharsets.UTF_8)),
                new Frame(0x12345678, (byte) 0b00000000, "1123456789".getBytes(StandardCharsets.UTF_8)),
                new Frame(0x12345678, (byte) 0b01000000, "212345678".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testEncodeWith4Frames() {
        givenAndExpects(0x12345678, 10, "012345678911234567892123456789312345678",
                new Frame(0x12345678, (byte) 0b10000000, "0123456789".getBytes(StandardCharsets.UTF_8)),
                new Frame(0x12345678, (byte) 0b00000000, "1123456789".getBytes(StandardCharsets.UTF_8)),
                new Frame(0x12345678, (byte) 0b00000000, "2123456789".getBytes(StandardCharsets.UTF_8)),
                new Frame(0x12345678, (byte) 0b01000000, "312345678".getBytes(StandardCharsets.UTF_8)));
    }

    private void givenAndExpects(int magic, int frameSize, String given, Frame... expectFrames) {
        EmbeddedChannel channel = new EmbeddedChannel(new BlockToByteEncoder(magic, frameSize));

        ByteSlice data = new ByteSlice(given.getBytes(StandardCharsets.UTF_8));

        Assertions.assertTrue(channel.writeOutbound(data));

        ByteBuf x = channel.readOutbound();
        byte[] dataGot = new byte[x.readableBytes()];
        x.readBytes(dataGot);

        List<byte[]> bytesList = new ArrayList<>();
        for (Frame frame : expectFrames) {
            byte[] bytes = frame.getBytes(true, true);
            bytesList.add(bytes);
        }

        int expectBytesLen = bytesList.stream().map(bytes -> bytes.length).reduce(0, Integer::sum);
        ByteBuffer bb = ByteBuffer.allocate(expectBytesLen);
        for (byte[] bytes : bytesList) {
            bb.put(bytes);
        }
        byte[] expectBytes = bb.array();

        Assertions.assertArrayEquals(expectBytes, dataGot);
    }
}