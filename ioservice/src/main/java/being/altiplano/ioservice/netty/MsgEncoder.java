package being.altiplano.ioservice.netty;

import being.altiplano.config.Msg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by gaoyuan on 01/03/2017.
 */
class MsgEncoder extends MessageToByteEncoder<Msg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Msg msg, ByteBuf out) throws Exception {
        out.writeInt(msg.code);
        out.writeInt(msg.length);
        out.writeBytes(msg.data);
    }
}
