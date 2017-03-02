package being.altiplano.ioservice;

import being.altiplano.config.Command;
import being.altiplano.config.Reply;
import being.altiplano.config.commands.*;
import being.altiplano.config.replies.*;

import java.io.IOException;

/**
 * Created by gaoyuan on 23/02/2017.
 */
public abstract class AbstractClient implements IClient {
    protected final String address;
    protected final int port;

    public AbstractClient(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public final void close() throws IOException {
        try {
            disConnect();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    protected abstract <T extends Reply> T writeAndRead(Command command) throws IOException;

    @Override
    public StartReply call(StartCommand command) throws IOException {
        return writeAndRead(command);
    }

    @Override
    public StopReply call(StopCommand command) throws IOException {
        return writeAndRead(command);
    }

    @Override
    public EchoReply call(EchoCommand command) throws IOException {
        return writeAndRead(command);
    }

    @Override
    public CountReply call(CountCommand command) throws IOException {
        return writeAndRead(command);
    }

    @Override
    public ReverseReply call(ReverseCommand command) throws IOException {
        return writeAndRead(command);
    }

    @Override
    public LowerCastReply call(LowerCastCommand command) throws IOException {
        return writeAndRead(command);
    }

    @Override
    public UpperCastReply call(UpperCastCommand command) throws IOException {
        return writeAndRead(command);
    }
}
