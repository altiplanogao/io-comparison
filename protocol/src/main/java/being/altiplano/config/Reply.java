package being.altiplano.config;

/**
 * Created by gaoyuan on 21/02/2017.
 */
public interface Reply {
    int code();

    byte[] toBytes();
}
