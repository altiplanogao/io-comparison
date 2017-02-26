package being.altiplano.ioservice.aio;

import being.altiplano.config.Msg;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Created by gaoyuan on 26/02/2017.
 */
public class MsgReader {
    static final int bufferSize = 16; //must > 8
    private final AsynchronousSocketChannel channel;
    private final ByteBuffer buffer;

    class ReadPackage implements CompletionHandler<Integer, ReadPackage> {
        private boolean headDone = false;
        private int code = 0;
        private int len = 0;
        private byte[] data = null;
        private int dataOffset;
        private int bytesRead = 0;

        @Override
        public void completed(Integer result, ReadPackage attachment) {
            if (result < 0) {
                return;
            } else {
                bytesRead += result;
            }
            if (bytesRead < 8) {
                channel.read(buffer, this, this);
                return;
            }

            buffer.flip();
            if (!headDone) {
                if (bytesRead >= 8) {
                    code = buffer.getInt();
                    len = buffer.getInt();
                    data = new byte[len];
                    headDone = true;
                }
            }

            int cpyLen = buffer.remaining();
            buffer.get(data, dataOffset, cpyLen);
            dataOffset += cpyLen;
            buffer.clear();

            if ((!headDone) || (len != dataOffset)) {
                channel.read(buffer, this, this);
            } else {
                Msg msg = new Msg(code, data);
                onMsg(msg);
            }
        }

        @Override
        public void failed(Throwable exc, ReadPackage attachment) {
            exc.printStackTrace();
        }
    }

    MsgReader(AsynchronousSocketChannel ch) {
        this.channel = ch;
        buffer = ByteBuffer.allocate(bufferSize);
    }

    public void read() {
        ReadPackage pack = new ReadPackage();
        channel.read(buffer, pack, pack);
    }

    protected void onMsg(Msg msg) {

    }
}
