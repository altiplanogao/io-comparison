package being.altiplano.config.commands;

/**
 * Created by gaoyuan on 21/02/2017.
 */
public class ReverseCommand extends ContentCommand {
    public ReverseCommand(byte[] data) {
        super(data);
    }

    public ReverseCommand(String data) {
        super(data);
    }

    public int code() {
        return REVERSE;
    }
}
