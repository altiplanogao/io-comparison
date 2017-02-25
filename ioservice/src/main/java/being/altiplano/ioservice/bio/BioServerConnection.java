package being.altiplano.ioservice.bio;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.IServerConnection;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by gaoyuan on 21/02/2017.
 */
class BioServerConnection implements IServerConnection, Closeable {
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream out;

    public BioServerConnection(Socket socket) throws IOException {
        this.socket = socket;
        input = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public Command readCommand() throws IOException {
        Msg msg = null;
        synchronized (input) {
            msg = BioStreamHelper.readMsg(input);
        }
        return MsgConverter.convert(msg);
    }

    @Override
    public void writeReply(Reply reply) throws IOException {
        synchronized (out) {
            int code = reply.code();
            byte[] data = reply.toBytes();
            BioStreamHelper.write(out, code, data);
        }
    }

    public void close() throws IOException {
        if (socket != null) {
            socket.close();
            input.close();
            out.close();
        }
    }
}
