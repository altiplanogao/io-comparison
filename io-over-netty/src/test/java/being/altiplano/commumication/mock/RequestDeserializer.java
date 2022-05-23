package being.altiplano.commumication.mock;

import being.altiplano.commumication.protocol.Deserializer;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

public class RequestDeserializer implements Deserializer<MRequest> {
    protected Gson gson = new Gson();
    @Override
    public MRequest deserialize(byte[] bytes) {
        String json = new String(bytes, StandardCharsets.UTF_8);
        return gson.fromJson(json, MRequest.class);
    }
}
