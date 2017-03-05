package being.altiplano.ioservice.aio;

import being.altiplano.config.Msg;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * Write {@link Msg} to {@link AsynchronousSocketChannel}.
 */
class MsgWriter {
    static final int bufferSize = 16; //must > 8
    private final AsynchronousSocketChannel channel;
    private final ByteBuffer buffer;

    class WritePackage implements CompletionHandler<Integer, WritePackage> {
        private final int code;
        private final int dataLen;
        private final byte[] data;
        private final int totalToWrite;

        private boolean headDone = false;
        private int dataOffset;
        private int bytesWritten = 0;

        public WritePackage(Msg msg) {
            code = msg.code;
            dataLen = msg.length;
            data = msg.data;
            totalToWrite = dataLen + 8;
        }

        @Override
        public void completed(Integer result, WritePackage attachment) {
            if (result > 0) {
                bytesWritten += result;
            }
            if (bytesWritten < totalToWrite) {
                fillBuffer();
                channel.write(buffer, this, this);
            } else if (bytesWritten == totalToWrite) {
                onWriteDone();
            } else {
                throw new IllegalStateException();
            }
        }

        private void fillBuffer() {
            buffer.clear();
            if (!headDone) {
                buffer.putInt(code).putInt(dataLen);
                headDone = true;
            }
            int dataRemain = dataLen - dataOffset;
            int bufferRemain = buffer.remaining();
            int cpyLen = Math.min(dataRemain, bufferRemain);
            buffer.put(data, dataOffset, cpyLen);
            dataOffset += cpyLen;
            buffer.flip();
        }

        @Override
        public void failed(Throwable exc, WritePackage attachment) {
            exc.printStackTrace();
        }
    }

    MsgWriter(AsynchronousSocketChannel ch) {
        this.channel = ch;
        buffer = ByteBuffer.allocate(bufferSize);
    }

    public void write(Msg msg) {
        WritePackage pack = new WritePackage(msg);
        pack.fillBuffer();
        channel.write(buffer, pack, pack);
    }

    protected void onWriteDone() {

    }
}
