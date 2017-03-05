package being.altiplano.ioservice.bio;

import being.altiplano.config.Msg;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Helper class
 */
class BioStreamHelper {
    public static void write(DataOutputStream out, int code, byte[] data) throws IOException {
        final int length = data.length;
        out.writeInt(code);
        out.writeInt(data.length);
        int offset = 0;
        int remain = length - offset;
        out.write(data, offset, remain);
    }

    public static Msg readMsg(DataInputStream input) throws IOException {
        Msg msg;
        final int code = input.readInt();
        byte[] bytes = null;
        final int length = input.readInt();
        if (length > 0) {
            bytes = new byte[length];
            int offset = 0;
            int remain = length - offset;
            while (remain != 0) {
                offset += input.read(bytes, offset, remain);
                remain = length - offset;
            }
        }
        msg = new Msg(code, bytes);
        return msg;
    }

}
