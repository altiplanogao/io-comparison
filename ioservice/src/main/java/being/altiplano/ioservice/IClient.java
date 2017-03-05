package being.altiplano.ioservice;

import being.altiplano.config.commands.*;
import being.altiplano.config.replies.*;

import java.io.Closeable;
import java.io.IOException;

/**
 * A socket client
 * IClient provide functions:
 * 1. connect
 * 2. disconnect
 * also a list of command-and-reply methods:
 * 1. start
 * 2. stop
 * 3. echo
 * 4. count
 * 5. reverse
 * 6. lower-cast
 * 7. upper-cast
 */
public interface IClient extends Closeable {
    /**
     * Publish a connection.
     * @throws IOException
     * @throws InterruptedException
     */
    void connect() throws IOException, InterruptedException;

    /**
     * Disconnect
     *
     * @throws IOException
     * @throws InterruptedException
     */
    void disconnect() throws IOException, InterruptedException;

    StartReply call(StartCommand command) throws IOException;

    StopReply call(StopCommand command) throws IOException;

    EchoReply call(EchoCommand command) throws IOException;

    CountReply call(CountCommand command) throws IOException;

    ReverseReply call(ReverseCommand command) throws IOException;

    LowerCastReply call(LowerCastCommand command) throws IOException;

    UpperCastReply call(UpperCastCommand command) throws IOException;
}
