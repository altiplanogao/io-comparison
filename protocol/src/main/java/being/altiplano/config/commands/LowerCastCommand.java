package being.altiplano.config.commands;

/**
 * A request to do a lower-cast operation to a string.
 * Calling this command to the server will result in a {@link being.altiplano.config.replies.LowerCastReply} in return.
 * Example: "ABC" -> "abc"
 *
 * @see being.altiplano.config.replies.LowerCastReply
 */
public class LowerCastCommand extends ContentCommand {
    public LowerCastCommand(byte[] data) {
        super(data);
    }

    public LowerCastCommand(String data) {
        super(data);
    }

    public int code() {
        return LOWER_CAST;
    }
}
