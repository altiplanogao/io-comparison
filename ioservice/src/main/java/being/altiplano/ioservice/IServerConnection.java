package being.altiplano.ioservice;

import being.altiplano.config.Command;
import being.altiplano.config.Reply;

import java.io.IOException;

public interface IServerConnection {
    Command readCommand() throws IOException;

    void writeReply(Reply reply) throws IOException;
}
