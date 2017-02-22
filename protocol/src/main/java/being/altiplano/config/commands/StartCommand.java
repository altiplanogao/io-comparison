package being.altiplano.config.commands;

import com.google.gson.Gson;

/**
 * Created by gaoyuan on 21/02/2017.
 */
public class StartCommand extends ContentCommand {
    public StartCommand(byte[] data) {
        super(data);
    }

    public StartCommand(String data) {
        super(data);
    }

    public StartCommand(Config config){
        this(new Gson().toJson(config));
    }

    public int code() {
        return START;
    }

    public static class Config {
        public long lag;
    }
}
