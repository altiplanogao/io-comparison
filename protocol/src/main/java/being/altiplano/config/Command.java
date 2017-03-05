package being.altiplano.config;

/**
 * Command interface
 */
public interface Command {
    int START = 1;
    int STOP = 2;
    int ECHO = 3;
    int COUNT = 4;
    int REVERSE = 5;
    int UPPER_CAST = 6;
    int LOWER_CAST = 7;

    /**
     * @return the command code
     */
    int code();

    /**
     * The data body of the command.
     * @return the data body
     */
    byte[] toBytes();

    /**
     * convert the Command to {@link Msg}
     * @return the {@link Msg}
     */
    default Msg toMsg() {
        return new Msg(code(), toBytes());
    }
}
