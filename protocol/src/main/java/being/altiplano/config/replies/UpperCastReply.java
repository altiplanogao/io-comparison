package being.altiplano.config.replies;

import being.altiplano.config.Command;

/**
 * @see being.altiplano.config.commands.UpperCastCommand
 */
public class UpperCastReply extends ContentReply {
    public UpperCastReply(String content) {
        super(content);
    }

    public UpperCastReply(byte[] data) {
        super(data);
    }

    public int code() {
        return Command.UPPER_CAST;
    }
}
