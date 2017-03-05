package being.altiplano.config.commands;

import com.google.gson.Gson;

/**
 * A request to start the connection.
 * Calling this command to the server will result in a {@link being.altiplano.config.replies.StartReply} in return.
 *
 * @see being.altiplano.config.replies.StartReply
 */
public class StartCommand extends ContentCommand {
    public StartCommand(byte[] data) {
        super(data);
    }

    public StartCommand(String data) {
        super(data);
    }

    public StartCommand(Config config) {
        this(new Gson().toJson(config));
    }

    public int code() {
        return START;
    }

    public static class Config {
        public long lag;
    }
}
