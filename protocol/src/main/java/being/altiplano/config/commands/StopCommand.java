package being.altiplano.config.commands;

import being.altiplano.config.Command;

/**
 * A request to stop the connection.
 * Calling this command to the server will result in a {@link being.altiplano.config.replies.StopReply} in return.
 *
 * @see being.altiplano.config.replies.StopReply
 */
public class StopCommand implements Command {
    public int code() {
        return STOP;
    }

    public byte[] toBytes() {
        return new byte[0];
    }
}
