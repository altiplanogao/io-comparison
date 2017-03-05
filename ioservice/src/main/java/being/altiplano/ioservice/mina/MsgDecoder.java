package being.altiplano.ioservice.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * Created by gaoyuan on 28/02/2017.
 */
class MsgDecoder extends CumulativeProtocolDecoder {

    private final AttributeKey PACK = new AttributeKey(getClass(), "pack");

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        final Pack pack = getPack(session);
        if (!pack.headDone) {
            if (in.remaining() < 8) {
                return false;
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
        requires = pack.requires();
        if (requires == 0) {
            out.write(pack.toMsg());
            pack.reset();
            return true;
        }

        return false;
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