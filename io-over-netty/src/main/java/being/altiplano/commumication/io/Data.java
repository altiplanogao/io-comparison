package being.altiplano.commumication.io;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * A reference to a slice in bytes array
 */
class Slice {
    public final byte[] data;

    public final int offset;

    public final int length;

    /**
     * @param data
     */
    public Slice(byte[] data) {
        this(data, 0, data.length);
    }

    public Slice(byte[] data, int offset, int length) {
        this.data = data;
        if (offset < 0 || length <= 0) {
            throw new IllegalArgumentException();
        }
        if (data.length < offset + length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        this.offset = offset;
        this.length = length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Slice slice = (Slice) o;
        if (this.isAllBytesUsed() && slice.isAllBytesUsed()) {
            return Arrays.equals(data, slice.data);
        }
        return Arrays.equals(getUsedBytes(), slice.getUsedBytes());
    }

    private boolean isAllBytesUsed() {
        return offset == 0 && length == data.length;
    }

    byte[] getUsedBytes() {
        if (isAllBytesUsed()) {
            return data;
        }
        byte[] dataCopy = new byte[length];
        System.arraycopy(data, offset, dataCopy, 0, length);
        return dataCopy;
    }

    @Override
    public int hashCode() {
        if (isAllBytesUsed()) {
            return Arrays.hashCode(data);
        }
        return Arrays.hashCode(getUsedBytes());
    }
}

class Block {
    public final byte[] data;

    public Block(byte[] data) {
        this.data = data;
    }
}

class Frame {
    static final byte INTEGRAL_MASK = (byte) 0b11000000;
    static final byte IS_HEAD_MASK = (byte) 0b10000000;
    static final byte IS_TAIL_MASK = (byte) 0b01000000;
    static final byte IS_MID_MASK = (byte) 0b00000000;

    static final int HEAD_LEN = 9;
    /**
     * 协议魔数
     */
    private final int magic;
    /**
     * 协议控制byte，8个比特位分别记为：abcdefgh（从高到低）
     * ab位表示Frame是否能表达一个完整的用户消息，cdefgh尚未使用
     * a为0b1：这个Frame包含用户消息头
     * a为0b0：这个Frame不包含用户消息头
     * b为0b1：这个Frame包含用户消息尾
     * b为0b0：这个Frame不包含用户消息尾
     * <p>
     * control 为 0b11xxxxxx：这个Frame能表达完整的用户消息
     * control 为 0b10xxxxxx：这个Frame不能表达完整的用户消息，但消息头部在这个Frame中
     * control 为 0b01xxxxxx：这个Frame不能表达完整的用户消息，但消息尾部在这个Frame中
     * control 为 0b00xxxxxx：这个Frame不能表达完整的用户消息，消息头尾部都不在这个Frame中
     * <p>
     * 假设用户发送一个短消息"ABC"，它会在单个Frame中被发送，其control 值为0b11xxxxxx。
     * 假设用户发送一个长的消息"AAA... BBB... CCC... DDD..."，会被分解为4个Frame, 其control 值依次为：
     * 0b10xxxxxx、0b00xxxxxx、0b00xxxxxx、0b01xxxxxx
     */
    private final byte control;
    /**
     * 数据长度
     */
    private final int bodyLength;
    /**
     * 数据
     */
    private Slice body;

    public Frame(int magic, byte control, int bodyLength) {
        this.magic = magic;
        this.bodyLength = bodyLength;
        this.control = control;
    }

    public Frame(int magic, byte control, Slice body) {
        this.magic = magic;
        this.control = control;
        this.bodyLength = body.length;
        this.body = body;
    }

    public Frame(int magic, byte control, byte[] body) {
        this(magic, control, body, true);
    }

    public Frame(int magic, byte control, byte[] body, boolean doCopy) {
        this.magic = magic;
        this.control = control;
        this.bodyLength = body.length;
        byte[] bodyBytes = body;
        if (doCopy) {
            bodyBytes = body.clone();
        }
        this.body = new Slice(bodyBytes);
    }

    public int getMagic() {
        return magic;
    }

    public byte getControl() {
        return control;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    Slice getBody() {
        return body;
    }

    Frame setBody(Slice body) {
        if (body.length != bodyLength) {
            throw new IllegalArgumentException("array size mismatch");
        }
        this.body = body;
        return this;
    }

    Frame setBody(byte[] body, boolean doCopy) {
        Slice slice = new Slice(doCopy ? body.clone() : body);
        return setBody(slice);
    }

    public boolean isIntegral() {
        return (control & INTEGRAL_MASK) == INTEGRAL_MASK;
    }

    public boolean containsHead() {
        return (control & IS_HEAD_MASK) == IS_HEAD_MASK;
    }

    public boolean containsTail() {
        return (control & IS_TAIL_MASK) == IS_TAIL_MASK;
    }

    public static Frame makeIntegral(Frame... frames) {
        if (frames.length <= 0) {
            throw new IllegalArgumentException();
        }
        final Frame first = frames[0];
        final Frame last = frames[frames.length - 1];
        // check whether it can return directly
        if (frames.length == 1) {
            if ((first.control & INTEGRAL_MASK) != INTEGRAL_MASK) {
                return first;
            }
        }

        // check control
        if ((first.control & IS_HEAD_MASK) != IS_HEAD_MASK) {
            throw new IllegalArgumentException("First Frame should be match head mask (0b10xxxxxx)");
        }
        if ((last.control & IS_TAIL_MASK) != IS_TAIL_MASK) {
            throw new IllegalArgumentException("Last Frame should be match tail mask (0b01xxxxxx)");
        }
        for (int i = 1; i < frames.length - 1; ++i) {
            Frame frame = frames[i];
            if ((frame.control & INTEGRAL_MASK) != IS_MID_MASK) {
                throw new IllegalArgumentException("Mid frame should be match mid mask (0b00xxxxxx)");
            }
        }

        // check magic
        final int magic = first.magic;
        boolean magicMatch = Arrays.stream(frames).allMatch(Frame -> Frame.magic == magic);
        if (!magicMatch) {
            throw new IllegalArgumentException("Magic code mis-match");
        }

        // merge body
        final int len = Arrays.stream(frames).map(p -> p.bodyLength).reduce(0, Integer::sum);
        byte[] result = new byte[len];
        int offset = 0;
        for (Frame frame : frames) {
            Slice src = frame.body;
            System.arraycopy(src.data, src.offset, result, offset, src.length);
            offset += frame.bodyLength;
        }
        Slice resultSlice = new Slice(result, 0, len);

        return new Frame(magic, (byte) (first.control | INTEGRAL_MASK), resultSlice);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Frame frame = (Frame) o;
        return magic == frame.magic && control == frame.control && bodyLength == frame.bodyLength && Objects.equals(body, frame.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(magic, control, bodyLength, body);
    }

    byte[] headBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(HEAD_LEN);
        buffer.asIntBuffer().put(magic).put(bodyLength);
        buffer.position(8);
        buffer.put(control);
        return buffer.array();
    }

    byte[] getBytes(boolean includeHead, boolean includeBody) {
        int resultLen = 0;
        if (includeHead) {
            resultLen += HEAD_LEN;
        }
        if (includeBody) {
            resultLen += bodyLength;
        }
        if (resultLen == 0) {
            return new byte[0];
        }
        ByteBuffer buffer = ByteBuffer.allocate(resultLen);
        if (includeHead) {
            buffer.asIntBuffer().put(magic).put(bodyLength);
            buffer.position(8);
            buffer.put(control);
        }
        if (includeBody) {
            buffer.put(body.data, body.offset, body.length);
        }
        return buffer.array();
    }

    @Override
    public String toString() {
        String ctrStr = Integer.toBinaryString(control & 0xFF);
        int contentClip = Math.min(100, bodyLength);
        String partial = body == null ? "" : new String(body.getUsedBytes(), 0, contentClip, StandardCharsets.UTF_8);
        return String.format("0x%X CTL:%s LEN:%d BODY:%s%s", magic, ctrStr, bodyLength, partial, (contentClip < bodyLength ? " ..." : ""));
    }
}