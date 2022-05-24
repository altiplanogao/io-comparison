package being.altiplano.commumication.protocol;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncClient<REQUEST, RESPONSE> {
    private static class ResponseFuture<REQUEST, RESPONSE> extends CompletableFuture<RESPONSE> {
        private final REQUEST request;

        public ResponseFuture(REQUEST request) {
            this.request = request;
        }

        @Override
        public String toString() {
            RESPONSE response = super.getNow(null);
            return request.toString() + " RESPONSE:" + response + " " + super.toString();
        }
    }
    private final Client<REQUEST, RESPONSE> client;

    private final Queue<ResponseFuture<REQUEST, RESPONSE>> pending = new ConcurrentLinkedDeque<>();

    private final Listener<RESPONSE> responseListener;

    private final AtomicInteger requests = new AtomicInteger(0);

    public SyncClient(Client<REQUEST, RESPONSE> client) {
        this.client = client;
        this.responseListener = this::onResponse;
     }

    private void onResponse(RESPONSE response) {
        ResponseFuture<REQUEST, RESPONSE> future = pending.poll();
        if(future == null){
            return;
        }
        synchronized (this) {
            future.complete(response);
            if (requests.decrementAndGet() == 0) {
                client.removeResponseListener(responseListener);
            }
        }
    }

    public Future<RESPONSE> request(REQUEST request) {
        final ResponseFuture<REQUEST, RESPONSE> future;
        synchronized (this) {
            if (requests.getAndIncrement() == 0) {
                this.client.addResponseListener(responseListener);
            }
            future = new ResponseFuture<>(request);
            pending.add(future);
        }

        client.request(request);
        return future;
    }
}
