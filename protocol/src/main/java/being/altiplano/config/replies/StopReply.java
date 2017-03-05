package being.altiplano.config.replies;

import being.altiplano.config.Command;
import being.altiplano.config.Reply;

/**
 * @see being.altiplano.config.commands.StopCommand
 */
public class StopReply implements Reply {
    public int code() {
        return Command.STOP;
    }

    public byte[] toBytes() {
        return new byte[0];
    }
}
