package being.altiplano.config.commands;

/**
 * Created by gaoyuan on 21/02/2017.
 */
public class LowerCastCommand extends ContentCommand {
    public LowerCastCommand(byte[] data) {
        super(data);
    }

    public LowerCastCommand(String data) {
        super(data);
    }

    public int code() {
        return LOWER_CAST;
    }
}
