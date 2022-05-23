package being.altiplano.commumication.mock;

import being.altiplano.commumication.protocol.Serializer;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

public class RequestSerializer implements Serializer<MRequest> {
    protected Gson gson = new Gson();

    @Override
    public byte[] serialize(MRequest data) {
        String json = gson.toJson(data);
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
