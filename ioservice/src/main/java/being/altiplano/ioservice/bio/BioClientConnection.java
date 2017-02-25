package being.altiplano.ioservice.bio;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.IClientConnection;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by gaoyuan on 22/02/2017.
 */
class BioClientConnection implements IClientConnection, Closeable {
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream out;

    public BioClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        input = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public Reply readReply() throws IOException {
        final Msg msg;
        synchronized (input) {
            msg = BioStreamHelper.readMsg(input);
        }
        return MsgConverter.convertReply(msg);
    }

    @Override
    public void writeCommand(Command cmd) throws IOException {
        synchronized (out) {
            int code = cmd.code();
            byte[] data = cmd.toBytes();
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
