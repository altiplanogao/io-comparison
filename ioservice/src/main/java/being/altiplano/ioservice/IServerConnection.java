package being.altiplano.ioservice;

import being.altiplano.config.Command;
import being.altiplano.config.Reply;

import java.io.IOException;

/**
 * Created by gaoyuan on 24/02/2017.
 */
public interface IServerConnection {
    Command readCommand() throws IOException;

    void writeReply(Reply reply) throws IOException;
}
