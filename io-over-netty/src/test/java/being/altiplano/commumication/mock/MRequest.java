package being.altiplano.commumication.mock;

import java.util.Objects;

public class MRequest {
    public MCmdCode code;
    public int sn;
    public String data;

    public MRequest() {
    }

    public MRequest(int sn, MCmdCode code, String data) {
        this.sn = sn;
        this.code = code;
        this.data = data;
    }

    public int getSn() {
        return sn;
    }

    public MRequest setSn(int sn) {
        this.sn = sn;
        return this;
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
        MRequest request = (MRequest) o;
        return sn == request.sn && code == request.code && Objects.equals(data, request.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, sn, data);
    }

    @Override
    public String toString() {
        return "#" + sn + " " + code + ": \"" + data + '"';
    }
}
