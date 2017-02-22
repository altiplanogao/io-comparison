package being.altiplano.config.replies;

import being.altiplano.config.Command;
import being.altiplano.config.Reply;

/**
 * Created by gaoyuan on 21/02/2017.
 */
public class StartReply implements Reply {
    public int code() {
        return Command.START;
    }

    public byte[] toBytes() {
        return new byte[0];
    }
}
