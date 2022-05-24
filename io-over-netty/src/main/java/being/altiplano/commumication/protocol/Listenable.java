package being.altiplano.commumication.protocol;

import java.util.ArrayList;
import java.util.List;

public class Listenable<T> {
    private final List<Listener<T>> listeners = new ArrayList<>();

    public synchronized void addListener(Listener<T> listener) {
        listeners.add(listener);
    }

    public synchronized boolean removeListener(Listener<T> listener) {
        return listeners.remove(listener);
    }

    public final void fire(T event) {
        synchronized (this) {
            Listener<T>[] listenersArray = listeners.toArray(new Listener[0]);
            for (Listener<T> listener : listenersArray) {
                listener.onEvent(event);
            }
        }
    }
}
