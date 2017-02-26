package being.altiplano.ioservice.aio;

import being.altiplano.config.Command;
import being.altiplano.config.Msg;
import being.altiplano.config.MsgConverter;
import being.altiplano.config.Reply;
import being.altiplano.ioservice.ServerCommandHandler;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * Created by gaoyuan on 24/02/2017.
 */
public class AioServerConnection {
    private final AsynchronousSocketChannel channel;
    private final ServerCommandHandler commandHandler = new ServerCommandHandler();
    private final MsgReader reader;
    private final MsgWriter writer;

    public AioServerConnection(AsynchronousSocketChannel channel) {
        this.channel = channel;
        this.reader = new MsgReader(channel) {
            @Override
            protected void onMsg(Msg msg) {
                Command command = MsgConverter.convert(msg);
                Reply reply = commandHandler.handle(command);
                writer.write(reply.toMsg());
            }
        };
        this.writer = new MsgWriter(channel) {
            @Override
            protected void onWriteDone() {
                super.onWriteDone();
                processCommand();
            }
        };
    }

    public void processCommand() {
        reader.read();
    }
}
