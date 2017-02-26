package being.altiplano.ioservice.nio;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.IClientConnection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by gaoyuan on 25/02/2017.
 */
class NioClientConnection implements IClientConnection {
    private SocketChannel channel;
    ByteBuffer r_header = ByteBuffer.allocate(8);
    ByteBuffer r_body = ByteBuffer.allocate(16);
    ByteBuffer w_header = ByteBuffer.allocate(8);
    ByteBuffer w_body = ByteBuffer.allocate(16);

    public NioClientConnection(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public Reply readReply() throws IOException {
        Msg msg = NioChannelHelper.readMsg(channel, r_header, r_body);
        return MsgConverter.convertReply(msg);
    }

    @Override
    public void writeCommand(Command cmd) throws IOException {
        int code = cmd.code();
        byte[] data = cmd.toBytes();
        NioChannelHelper.writeMsg(channel, w_header, w_body, code, data);
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
