package being.altiplano.ioservice.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Helper class of SocketChannel
 */
class BufferUtils {
    public static byte[] readChannel(SocketChannel channel,
                                     ByteBuffer buffer, final int requires)
            throws IOException {
        final int cap = buffer.capacity();
        byte[] data = new byte[requires];
        if (requires <= 0) {
            return data;
        }

        int done = 0;
        int stillRequires = requires;
        do {
            if (stillRequires >= cap) {
                while (buffer.hasRemaining()) {
                    channel.read(buffer);
                }
            } else {
                while (buffer.position() < stillRequires) {
                    channel.read(buffer);
                }
            }

            buffer.flip();
            int fresh = buffer.remaining();
            buffer.get(data, done, fresh);
            buffer.clear();

            stillRequires -= fresh;
            done += fresh;
        } while (stillRequires > 0);
        return data;
    }

    public static void writeChannel(SocketChannel channel,
                                    ByteBuffer buffer, byte[] data) throws IOException {
        final int cap = buffer.capacity();
        final int dataLen = data.length;
        if (dataLen == 0) {
            return;
        }
        int done = 0;
        int remainLen = dataLen;
        do {
            int roundCount = cap;
            if (remainLen < cap) {
                roundCount = remainLen;
            }
            buffer.put(data, done, roundCount);
            buffer.flip();
            do {
                channel.write(buffer);
            } while (buffer.hasRemaining());
            buffer.clear();
            remainLen -= roundCount;
            done += roundCount;
        } while (remainLen > 0);
    }
}


