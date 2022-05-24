package being.altiplano.commumication.protocol;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class SyncClient<REQUEST, RESPONSE, SN> {
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

    private final Function<REQUEST, SN> requestSnFunction;
    private final Function<RESPONSE, SN> responseSnFunction;

    private final Map<SN, ResponseFuture<REQUEST, RESPONSE>> pending = new ConcurrentHashMap<>();

    private final Listener<RESPONSE> responseListener;

    private final AtomicInteger requests = new AtomicInteger(0);

    public SyncClient(Client<REQUEST, RESPONSE> client,
                      Function<REQUEST, SN> requestSnFunction,
                      Function<RESPONSE, SN> responseSnFunction) {
        this.client = client;
        this.requestSnFunction = requestSnFunction;
        this.responseSnFunction = responseSnFunction;
        this.responseListener = this::onResponse;
     }

    private void onResponse(RESPONSE response) {
        SN sn = responseSnFunction.apply(response);
        pending.computeIfPresent(sn, (sn1, responseFuture) -> {
            responseFuture.complete(response);
            if (requests.decrementAndGet() == 0) {
                client.removeResponseListener(responseListener);
            }
            return null;
        });
    }

    public Future<RESPONSE> request(REQUEST request) {
        SN sn = requestSnFunction.apply(request);
        Future<RESPONSE> f = pending.compute(sn, (sn1, responseFuture) -> {
            if (responseFuture != null){
                throw new IllegalStateException("Duplicated request key");
            }
            if (requests.getAndIncrement() == 0){
                this.client.addResponseListener(responseListener);
            }
            client.request(request);
            return new ResponseFuture<>(request);
        });
        return f;
    }
}
