package being.altiplano.ioservice.bio;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.IClientAccess;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class BioClientAccess implements IClientAccess, Closeable {
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream out;

    public BioClientAccess(Socket socket) throws IOException {
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
        int code = cmd.code();
        byte[] data = cmd.toBytes();
        synchronized (out) {
            BioStreamHelper.write(out, code, data);
        }
    }

    public void close() throws IOException {
        socket.close();
        input.close();
        out.close();
    }
}
