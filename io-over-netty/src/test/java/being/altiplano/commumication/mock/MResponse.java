package being.altiplano.commumication.mock;

import java.util.Objects;

public class MResponse {
    private String data;
    public int sn;

    public MResponse(int sn, String data) {
        this.sn = sn;
        this.data = data;
    }

    public MResponse() {
        this(0, "");
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getSn() {
        return sn;
    }

    public MResponse setSn(int sn) {
        this.sn = sn;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MResponse response = (MResponse) o;
        return sn == response.sn && Objects.equals(data, response.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, sn);
    }

    @Override
    public String toString() {
        return "#" + sn + " \"" + data + '\"';
    }
}
