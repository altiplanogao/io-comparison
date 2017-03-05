package being.altiplano.config.commands;

import being.altiplano.config.MsgConverter;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * A request to do a echo operation to a string.
 * Calling this command to the server will result in a {@link being.altiplano.config.replies.EchoReply} in return.
 * Example: {"content":"abc.", "times":2} -> {"content":"abc.abc."}
 *
 * @see being.altiplano.config.replies.EchoReply
 */
public class EchoCommand extends ContentCommand {
    public EchoCommand(byte[] data) {
        super(data);
    }

    public EchoCommand(int times, String data) {
        this(convert(times, data));
    }

    public int code() {
        return ECHO;
    }

    public int times() {
        return ByteBuffer.wrap(data, 0, 4).getInt();
    }

    @Override
    public String getContent() {
        try {
            return new String(data, 4, data.length - 4, MsgConverter.ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static byte[] convert(int times, String data) {
        try {
            byte[] temp = data.getBytes(MsgConverter.ENCODING);
            return ByteBuffer.allocate(temp.length + 4).putInt(times).put(temp).array();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ByteBuffer.allocate(4).putInt(0).array();
        }
    }

    @Override
    public String toString() {
        return super.toString() + " x" + times();
    }
}
