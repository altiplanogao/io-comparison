package being.altiplano.config.replies;

import being.altiplano.config.Command;

/**
 * Created by gaoyuan on 21/02/2017.
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
