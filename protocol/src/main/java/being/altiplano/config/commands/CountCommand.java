package being.altiplano.config.commands;

/**
 * A request to count the length of a string.
 * Calling this command to the server will result in a {@link being.altiplano.config.replies.CountReply} in return.
 * Example : "abc" -> 3
 *
 * @see being.altiplano.config.replies.CountReply
 */
public class CountCommand extends ContentCommand {
    public CountCommand(byte[] data) {
        super(data);
    }

    public CountCommand(String data) {
        super(data);
    }

    public int code() {
        return COUNT;
    }
}
