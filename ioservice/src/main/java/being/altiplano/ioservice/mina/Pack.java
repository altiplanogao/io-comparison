package being.altiplano.ioservice.mina;

import being.altiplano.config.Msg;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Stateful msg information gather.
 */
class Pack {
    boolean headDone = false;
    private int code;
    private int len;
    private byte[] data = null;
    private int offset;

    void setHead(int code, int len) {
        if (!headDone) {
            this.code = code;
            this.len = len;
            this.data = new byte[len];
            this.offset = 0;
            this.headDone = true;
        } else {
            throw new IllegalStateException();
        }
    }

    int requires() {
        if (!headDone) {
            throw new IllegalStateException();
        }
        return len - offset;
    }

    void readBytes(IoBuffer in, final int readLen) {
        in.get(data, offset, readLen);
        offset += readLen;
    }

    void reset() {
        this.code = 0;
        this.len = 0;
        this.data = null;
        this.offset = 0;
        this.headDone = false;
    }

    Msg toMsg() {
        return new Msg(this.code, this.data);
    }

}
