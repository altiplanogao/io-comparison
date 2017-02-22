package being.altiplano.ioservice;

import being.altiplano.config.commands.*;
import being.altiplano.config.replies.*;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by gaoyuan on 23/02/2017.
 */
public interface IClient extends Closeable {
    void connect()throws IOException;

    void disConnect() throws IOException;

    StartReply call(StartCommand command) throws IOException;

    StopReply call(StopCommand command) throws IOException;

    EchoReply call(EchoCommand command) throws IOException;

    CountReply call(CountCommand command) throws IOException;

    ReverseReply call(ReverseCommand command) throws IOException;

    LowerCastReply call(LowerCastCommand command) throws IOException;

    UpperCastReply call(UpperCastCommand command) throws IOException;
}
