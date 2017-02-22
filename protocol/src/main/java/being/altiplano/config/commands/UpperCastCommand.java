package being.altiplano.config.commands;

/**
 * Created by gaoyuan on 21/02/2017.
 */
public class UpperCastCommand extends ContentCommand {
    public UpperCastCommand(byte[] data) {
        super(data);
    }

    public UpperCastCommand(String data) {
        super(data);
    }

    public int code() {
        return UPPER_CAST;
    }
}
