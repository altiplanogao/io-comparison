package being.altiplano.config;

/**
 * Reply interface
 */
public interface Reply {
    /**
     * @return the reply code
     */
    int code();

    /**
     * The data body of the reply.
     * @return the data body
     */
    byte[] toBytes();

    /**
     * convert the Reply to {@link Msg}
     * @return the {@link Msg}
     */
    default Msg toMsg() {
        return new Msg(code(), toBytes());
    }

}
