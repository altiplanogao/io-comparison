package being.altiplano.ioservice.nio;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.IServerConnection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by gaoyuan on 24/02/2017.
 */
class NioServerConnection implements IServerConnection {
    private final SocketChannel channel;
    ByteBuffer header = ByteBuffer.allocate(8);
    ByteBuffer body = ByteBuffer.allocate(16);

    public NioServerConnection(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public Command readCommand() throws IOException {
        Msg msg = NioChannelHelper.readMsg(channel, header, body);
        return MsgConverter.convert(msg);
    }

    @Override
    public void writeReply(Reply reply) throws IOException {
        int code = reply.code();
        byte[] data = reply.toBytes();
        NioChannelHelper.writeMsg(channel, header, body, code, data);
    }
}
