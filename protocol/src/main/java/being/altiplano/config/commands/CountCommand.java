package being.altiplano.config.commands;

/**
 * Created by gaoyuan on 21/02/2017.
 */
public class CountCommand extends ContentCommand {
    public CountCommand(byte[] data) {
        super(data);
    }

    public CountCommand(String data) {
        super(data);
    }

    public int code() {
        return COUNT;
    }
}
