package being.altiplano.config;

/**
 * Created by gaoyuan on 20/02/2017.
 */
public class Msg {
    public final int code;
    public final int length;
    public final byte[] data;

    public Msg(int code, byte[] data) {
        this.code = code;
        this.data = data;
        this.length = (data == null) ? 0 : data.length;
    }

}
