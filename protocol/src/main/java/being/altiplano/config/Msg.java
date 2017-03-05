package being.altiplano.config;

/**
 * Msg is a data structure that will be transferred through the socket.
 */
public class Msg {
    /**
     * the leading code, indicates the msg type.
     */
    public final int code;
    /**
     * the length of the {@link #data}
     */
    public final int length;
    /**
     * the msg body
     */
    public final byte[] data;

    public Msg(int code, byte[] data) {
        this.code = code;
        this.data = data;
        this.length = (data == null) ? 0 : data.length;
    }

}
