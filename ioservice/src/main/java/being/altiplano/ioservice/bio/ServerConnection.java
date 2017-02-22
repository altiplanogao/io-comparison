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
 * Created by gaoyuan on 21/02/2017.
 */
class ServerConnection implements Closeable {
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream out;

//    private final static int BUFFER_LENGTH = 16;
//    private final byte[] buffer = new byte[BUFFER_LENGTH];
//    private final int offset = 0;

    public ServerConnection(Socket socket) throws IOException {
        this.socket = socket;
        input = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    Command readCommand() throws IOException {
        final int code = input.readInt();
        byte[] bytes = null;
        synchronized (input) {
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
        }
        Msg msg = new Msg(code, bytes);
        return MsgConverter.convert(msg);
    }

    public void writeReply(Reply reply) throws IOException {
        synchronized (out) {
            int code = reply.code();
            byte[] data = reply.toBytes();
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
