package being.altiplano.commumication.mock;

import java.util.Objects;

public class MRequest {
    public MCmdCode code;
    public String data;

    public MRequest() {
    }

    public MRequest(MCmdCode code, String data) {
        this.code = code;
        this.data = data;
    }

    public MCmdCode getCode() {
        return code;
    }

    public MRequest setCode(MCmdCode code) {
        this.code = code;
        return this;
    }

    public String getData() {
        return data;
    }

    public MRequest setData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MRequest mRequest = (MRequest) o;
        return code == mRequest.code && Objects.equals(data, mRequest.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, data);
    }

    @Override
    public String toString() {
        return "" + code + ": \"" + data + '"';
    }
}
