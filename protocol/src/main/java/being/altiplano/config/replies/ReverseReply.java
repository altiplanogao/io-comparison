package being.altiplano.config.replies;

import being.altiplano.config.Command;

/**
 * @see being.altiplano.config.commands.ReverseCommand
 */
public class ReverseReply extends ContentReply {
    public ReverseReply(String content) {
        super(content);
    }

    public ReverseReply(byte[] data) {
        super(data);
    }

    public int code() {
        return Command.REVERSE;
    }
}
