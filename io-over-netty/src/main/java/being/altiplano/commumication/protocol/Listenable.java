package being.altiplano.commumication.protocol;

import java.util.ArrayList;
import java.util.List;

public class Listenable<T> {
    private final List<Listener<T>> listeners = new ArrayList<>();

    public void addListener(Listener<T> listener) {
        listeners.add(listener);
    }

    public boolean removeListener(Listener<T> listener) {
        return listeners.remove(listener);
    }

    public final void fire(T event) {
        for (Listener<T> listener : listeners) {
            listener.onEvent(event);
        }
    }
}
