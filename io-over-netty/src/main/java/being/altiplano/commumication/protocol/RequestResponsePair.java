package being.altiplano.commumication.protocol;

public class RequestResponsePair<REQUEST, RESPONSE> {
    private final REQUEST request;
    private final RESPONSE response;

    public RequestResponsePair(REQUEST request, RESPONSE response) {
        this.request = request;
        this.response = response;
    }

    public REQUEST getRequest() {
        return request;
    }

    public RESPONSE getResponse() {
        return response;
    }
}
