package being.altiplano.config;

/**
 * Created by gaoyuan on 21/02/2017.
 */
public interface Command {
    int START = 1;
    int STOP = 2;
    int ECHO = 3;
    int COUNT = 4;
    int REVERSE = 5;
    int UPPER_CAST = 6;
    int LOWER_CAST = 7;

    int code();

    byte[] toBytes();
}
