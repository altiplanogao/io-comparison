package being.altiplano.commumication.protocol;

public interface Listener<T> {
    void onEvent(T event);
}
