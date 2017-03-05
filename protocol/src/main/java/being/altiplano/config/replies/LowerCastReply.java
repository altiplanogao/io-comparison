package being.altiplano.config.replies;

import being.altiplano.config.Command;

/**
 * @see being.altiplano.config.commands.LowerCastCommand
 */
public class LowerCastReply extends ContentReply {
    public LowerCastReply(String content) {
        super(content);
    }

    public LowerCastReply(byte[] data) {
        super(data);
    }

    public int code() {
        return Command.LOWER_CAST;
    }
}
