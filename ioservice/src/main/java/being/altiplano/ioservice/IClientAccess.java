package being.altiplano.ioservice;

import being.altiplano.config.Command;
import being.altiplano.config.Reply;

import java.io.IOException;

/**
 * Easy operation wrapper
 */
public interface IClientAccess {
    Reply readReply() throws IOException;

    void writeCommand(Command cmd) throws IOException;
}
