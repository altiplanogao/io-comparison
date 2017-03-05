package being.altiplano.ioservice.mina;

import being.altiplano.config.Msg;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Created by gaoyuan on 27/02/2017.
 */
class MsgEncoder extends ProtocolEncoderAdapter {
    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        if (message instanceof Msg) {
            Msg msg = (Msg) message;
            IoBuffer buffer = IoBuffer.allocate(msg.length + 8, false)
                    .putInt(msg.code)
                    .putInt(msg.length)
                    .put(msg.data).flip();
            out.write(buffer);
        }
    }
}
