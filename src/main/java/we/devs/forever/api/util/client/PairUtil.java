package we.devs.forever.api.util.client;

public class PairUtil<T, S> {
    T key;
    S value;

    public PairUtil(T key, S value) {
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return this.key;
    }

    public void setKey(T key) {
        this.key = key;
    }

    public S getValue() {
        return this.value;
    }

    public void setValue(S value) {
        this.value = value;
    }
}
