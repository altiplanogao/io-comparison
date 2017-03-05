package being.altiplano.config.commands;

/**
 * A request to do a upper-cast operation to a string.
 * Calling this command to the server will result in a {@link being.altiplano.config.replies.UpperCastReply} in return.
 * Example: "abc" -> "ABC"
 *
 * @see being.altiplano.config.replies.UpperCastReply
 */
public class UpperCastCommand extends ContentCommand {
    public UpperCastCommand(byte[] data) {
        super(data);
    }

    public UpperCastCommand(String data) {
        super(data);
    }

    public int code() {
        return UPPER_CAST;
    }
}
