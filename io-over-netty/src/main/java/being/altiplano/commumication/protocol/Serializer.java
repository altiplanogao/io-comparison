package being.altiplano.commumication.protocol;

public interface Serializer<T> {
    byte[] serialize(T data);
}
