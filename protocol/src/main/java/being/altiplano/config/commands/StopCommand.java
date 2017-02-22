package being.altiplano.config.commands;

import being.altiplano.config.Command;

/**
 * Created by gaoyuan on 21/02/2017.
 */
public class StopCommand implements Command {
    public int code() {
        return STOP;
    }

    public byte[] toBytes() {
        return new byte[0];
    }
}
