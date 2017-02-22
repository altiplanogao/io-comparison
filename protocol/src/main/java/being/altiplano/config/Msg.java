package being.altiplano.config;

/**
 * Created by gaoyuan on 20/02/2017.
 */
public class Msg {
    final int code;
    final int length;
    final byte[] data;

    public Msg(int code, byte[] data) {
        this.code = code;
        this.data = data;
        this.length = (data == null) ? 0 : data.length;
    }

}
