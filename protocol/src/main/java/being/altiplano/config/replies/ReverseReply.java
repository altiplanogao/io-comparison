package being.altiplano.config.replies;

import being.altiplano.config.Command;

/**
 * Created by gaoyuan on 21/02/2017.
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
