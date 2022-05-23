package being.altiplano.commumication.protocol;

public interface Deserializer<T> {
    T deserialize(byte[] bytes);
}
