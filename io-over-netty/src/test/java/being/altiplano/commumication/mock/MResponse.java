package being.altiplano.commumication.mock;

import java.util.Objects;

public class MResponse {
    private String data;

    public MResponse(String data) {
        this.data = data;
    }

    public MResponse() {
        this("");
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MResponse mResponse = (MResponse) o;
        return Objects.equals(data, mResponse.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return data;
    }
}
