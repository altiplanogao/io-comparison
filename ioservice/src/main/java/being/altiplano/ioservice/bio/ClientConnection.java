package being.altiplano.ioservice.bio;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by gaoyuan on 22/02/2017.
 */
class ClientConnection implements Closeable {
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream out;

    public ClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        input = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    Reply readReply() throws IOException {
        Msg msg = null;
        synchronized (input) {
            final int code = input.readInt();
            byte[] bytes = null;
            final int length = input.readInt();
            if (length > 0) {
                bytes = new byte[length];
                int offset = 0;
                int remain = length - offset;
                while (remain != 0) {
                    offset += input.read(bytes, offset, remain);
                    remain = length - offset;
                }
            }
            msg = new Msg(code, bytes);
        }
        return MsgConverter.convertReply(msg);
    }

    public void writeCommand(Command cmd) throws IOException {
        synchronized (out) {
            int code = cmd.code();
            byte[] data = cmd.toBytes();
            final int length = data.length;
            out.writeInt(code);
            out.writeInt(data.length);
            int offset = 0;
            int remain = length - offset;
            out.write(data, offset, remain);
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
