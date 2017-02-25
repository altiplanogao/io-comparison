package being.altiplano.ioservice;

import being.altiplano.config.Command;
import being.altiplano.config.Reply;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by gaoyuan on 24/02/2017.
 */
public interface IClientConnection extends Closeable {
    Reply readReply() throws IOException;

    void writeCommand(Command cmd) throws IOException;
}
