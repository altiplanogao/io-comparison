package being.altiplano.ioservice.mina;

import being.altiplano.config.Msg;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * Created by gaoyuan on 28/02/2017.
 */
class MsgDecoderOld extends ProtocolDecoderAdapter {
    private static class Pack {
        private boolean headDone = false;
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

        void readBytes(IoBuffer in, int readLen) {
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

    private final AttributeKey PACK = new AttributeKey(getClass(), "pack");

    @Override
    public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        final Pack pack = getPack(session);
        if (!pack.headDone) {
            if (in.remaining() < 8) {
                in.compact();
                return;
            } else {
                int code = in.getInt();
                int len = in.getInt();
                pack.setHead(code, len);
            }
        }
        int requires = pack.requires();
        int remains = in.remaining();
        int cpyLen = Math.min(requires, remains);
        pack.readBytes(in, cpyLen);
        if (in.capacity() - in.position() < 8) {
            in.compact();
        }
        requires = pack.requires();
        if (requires == 0) {
            out.write(pack.toMsg());
            pack.reset();
        }
    }

    private Pack getPack(IoSession session) {
        Pack ctx;
        ctx = (Pack) session.getAttribute(PACK);

        if (ctx == null) {
            ctx = new Pack();
            session.setAttribute(PACK, ctx);
        }

        return ctx;
    }
}