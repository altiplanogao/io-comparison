package being.altiplano.config.commands;

/**
 * A request to do a reverse operation to a string.
 * Calling this command to the server will result in a {@link being.altiplano.config.replies.ReverseReply} in return.
 * Example: "abc" -> "cba"
 *
 * @see being.altiplano.config.replies.ReverseReply
 */
public class ReverseCommand extends ContentCommand {
    public ReverseCommand(byte[] data) {
        super(data);
    }

    public ReverseCommand(String data) {
        super(data);
    }

    public int code() {
        return REVERSE;
    }
}
