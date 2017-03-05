package being.altiplano.config.replies;

import being.altiplano.config.Command;

/**
 * @see being.altiplano.config.commands.EchoCommand
 */
public class EchoReply extends ContentReply {
    public EchoReply(String content) {
        super(content);
    }

    public EchoReply(byte[] data) {
        super(data);
    }

    public int code() {
        return Command.ECHO;
    }
}
