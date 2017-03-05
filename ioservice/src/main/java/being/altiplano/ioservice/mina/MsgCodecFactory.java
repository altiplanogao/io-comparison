package being.altiplano.ioservice.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

class MsgCodecFactory implements ProtocolCodecFactory {

    private final MsgDecoder msgDecoder = new MsgDecoder();
    private final MsgEncoder msgEncoder = new MsgEncoder();

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return msgEncoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return msgDecoder;
    }
}
