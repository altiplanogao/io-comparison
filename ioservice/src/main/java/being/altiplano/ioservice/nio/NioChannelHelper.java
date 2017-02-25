package being.altiplano.ioservice.nio;

import being.altiplano.config.Msg;
import being.altiplano.ioservice.utils.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by gaoyuan on 25/02/2017.
 */
public class NioChannelHelper {
    public static Msg readMsg(SocketChannel channel, ByteBuffer header, ByteBuffer body) throws IOException {
        ByteBuffer[] bufferArray = {header, body};
        do {
            channel.read(bufferArray);
        } while (header.hasRemaining());
        header.flip();
        int code = header.getInt();
        int len = header.getInt();
        header.clear();

        byte[] bytes = BufferUtils.readChannel(channel, body, len);
        return new Msg(code, bytes);
    }

    public static void writeMsg(SocketChannel channel, ByteBuffer header, ByteBuffer body, int code, byte[] data) throws IOException {
        final int length = data.length;
        header.putInt(code);
        header.putInt(length);
        header.flip();
        do {
            channel.write(header);
        } while (header.hasRemaining());
        header.clear();
        BufferUtils.writeChannel(channel, body, data);
    }
}
