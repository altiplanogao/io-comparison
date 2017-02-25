package being.altiplano.config.commands;

import being.altiplano.config.Command;
import being.altiplano.config.MsgConverter;

/**
 * Created by gaoyuan on 21/02/2017.
 */
abstract class ContentCommand implements Command {
    protected byte[] data;

    public ContentCommand(byte[] data) {
        this.data = data;
    }

    protected ContentCommand(String data) {
        this(MsgConverter.stringToBytes(data));
    }

    public String getContent() {
        return MsgConverter.bytesToString(data);
    }

    public byte[] toBytes() {
        return data;
    }

    @Override
    public String toString() {
        String content = getContent();
        return this.getClass().getSimpleName() +
                " \"" + content + "\"(" + content.length() + ")";
    }
}
