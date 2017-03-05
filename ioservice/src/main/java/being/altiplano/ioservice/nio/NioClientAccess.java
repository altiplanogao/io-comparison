package being.altiplano.ioservice.nio;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.IClientAccess;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class NioClientAccess implements IClientAccess {
    private SocketChannel channel;
    ByteBuffer r_header = ByteBuffer.allocate(8);
    ByteBuffer r_body = ByteBuffer.allocate(16);
    ByteBuffer w_header = ByteBuffer.allocate(8);
    ByteBuffer w_body = ByteBuffer.allocate(16);

    public NioClientAccess(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public Reply readReply() throws IOException {
        Msg msg;
        synchronized (channel) {
            msg = NioChannelHelper.readMsg(channel, r_header, r_body);
        }
        return MsgConverter.convertReply(msg);
    }

    @Override
    public void writeCommand(final Command cmd) throws IOException {
        int code = cmd.code();
        byte[] data = cmd.toBytes();
        synchronized (channel) {
            NioChannelHelper.writeMsg(channel, w_header, w_body, code, data);
        }
    }
}
