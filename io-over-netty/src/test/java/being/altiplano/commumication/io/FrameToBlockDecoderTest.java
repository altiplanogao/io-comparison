package being.altiplano.commumication.io;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class FrameToBlockDecoderTest {
    private final int magic = 0x12345678;

    @Test
    public void testDecodeNotFinish() {
        EmbeddedChannel channel = new EmbeddedChannel(new FrameToBlockDecoder(magic));

        Frame data0 = new Frame(magic, (byte) 0b10000000, "Hello World".getBytes(StandardCharsets.UTF_8));
        Assertions.assertFalse(channel.writeInbound(data0));

        Assertions.assertFalse(channel.finish());
        Block dataGot = channel.readInbound();

        Assertions.assertNull(dataGot);
    }

    @Test
    public void testDecodeOnce() {
        EmbeddedChannel channel = new EmbeddedChannel(new FrameToBlockDecoder(magic));

        Frame data0 = new Frame(magic, (byte) 0b11000000, "Hello World".getBytes(StandardCharsets.UTF_8));
        channel.writeInbound(data0);

        Assertions.assertTrue(channel.finish());
        Block dataGot = channel.readInbound();

        Assertions.assertArrayEquals(data0.getBody().getUsedBytes(), dataGot.data);
        Assertions.assertNull(channel.readInbound());
    }

    @Test
    public void testDecodeWith2Frame() {
        EmbeddedChannel channel = new EmbeddedChannel(new FrameToBlockDecoder(magic));

        Frame data0 = new Frame(magic, (byte) 0b10000000, "ABC".getBytes(StandardCharsets.UTF_8));
        Assertions.assertFalse(channel.writeInbound(data0));
        Frame data1 = new Frame(magic, (byte) 0b01000000, "DEF".getBytes(StandardCharsets.UTF_8));
        Assertions.assertTrue(channel.writeInbound(data1));

        Assertions.assertTrue(channel.finish());
        Block dataGot = channel.readInbound();

        Assertions.assertArrayEquals("ABCDEF".getBytes(StandardCharsets.UTF_8), dataGot.data);
        Assertions.assertNull(channel.readInbound());
    }

    @Test
    public void testDecodeWith1Result() {
        givenAndExpects(new Frame[]{
                        new Frame(magic, (byte) 0b11000000, "ABCDEF".getBytes(StandardCharsets.UTF_8))},
                "ABCDEF");
        givenAndExpects(new Frame[]{
                        new Frame(magic, (byte) 0b10000000, "ABC".getBytes(StandardCharsets.UTF_8)),
                        new Frame(magic, (byte) 0b01000000, "DEF".getBytes(StandardCharsets.UTF_8))},
                "ABCDEF");
        givenAndExpects(new Frame[]{
                        new Frame(magic, (byte) 0b10000000, "AB".getBytes(StandardCharsets.UTF_8)),
                        new Frame(magic, (byte) 0b00000000, "CD".getBytes(StandardCharsets.UTF_8)),
                        new Frame(magic, (byte) 0b01000000, "EF".getBytes(StandardCharsets.UTF_8))},
                "ABCDEF");
    }

    @Test
    public void testDecodeWith2Result() {
        givenAndExpects(new Frame[]{
                        new Frame(magic, (byte) 0b11000000, "ABC".getBytes(StandardCharsets.UTF_8)),
                        new Frame(magic, (byte) 0b11000000, "DEF".getBytes(StandardCharsets.UTF_8))},
                "ABC","DEF");
        givenAndExpects(new Frame[]{
                        new Frame(magic, (byte) 0b11000000, "ABC".getBytes(StandardCharsets.UTF_8)),
                        new Frame(magic, (byte) 0b10000000, "D".getBytes(StandardCharsets.UTF_8)),
                        new Frame(magic, (byte) 0b00000000, "E".getBytes(StandardCharsets.UTF_8)),
                        new Frame(magic, (byte) 0b01000000, "F".getBytes(StandardCharsets.UTF_8))},
                "ABC","DEF");
    }

    private void givenAndExpects(Frame[] given, String... expects) {
        EmbeddedChannel channel = new EmbeddedChannel(new FrameToBlockDecoder(magic));

        for (Frame frame : given) {
            channel.writeInbound(frame);
        }
        Assertions.assertTrue(channel.finish());

        List<String> resultList = new ArrayList<>();
        for (int i = 0; i < expects.length; i++) {
            Block dataGot = channel.readInbound();
            resultList.add(new String(dataGot.data, StandardCharsets.UTF_8));
        }
        String[] resultArray = resultList.toArray(new String[0]);
        Assertions.assertArrayEquals(expects, resultArray);

        Assertions.assertNull(channel.readInbound());
    }
}