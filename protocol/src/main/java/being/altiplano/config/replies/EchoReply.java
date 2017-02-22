package being.altiplano.config.replies;

import being.altiplano.config.Command;

/**
 * Created by gaoyuan on 21/02/2017.
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
