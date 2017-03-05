package being.altiplano.config.replies;

import being.altiplano.config.Command;
import being.altiplano.config.Reply;

/**
 * @see being.altiplano.config.commands.StartCommand
 */
public class StartReply implements Reply {
    public int code() {
        return Command.START;
    }

    public byte[] toBytes() {
        return new byte[0];
    }
}
