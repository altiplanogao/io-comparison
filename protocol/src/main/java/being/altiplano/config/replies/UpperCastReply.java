package being.altiplano.config.replies;

import being.altiplano.config.Command;

/**
 * Created by gaoyuan on 21/02/2017.
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
