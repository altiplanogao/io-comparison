package being.altiplano.commumication.mock;

import being.altiplano.commumication.protocol.Deserializer;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

public class ResponseDeserializer implements Deserializer<MResponse> {
    protected Gson gson = new Gson();
    @Override
    public MResponse deserialize(byte[] bytes) {
        String json = new String(bytes, StandardCharsets.UTF_8);
        return gson.fromJson(json, MResponse.class);
    }
}
