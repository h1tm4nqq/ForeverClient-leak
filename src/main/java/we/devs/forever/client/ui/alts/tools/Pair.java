package we.devs.forever.client.ui.alts.tools;

import java.io.Serializable;

public class Pair<V1, V2>
        implements Serializable {
    private static final long serialVersionUID = 2586850598481149380L;
    private  V1 obj1;
    private  V2 obj2;

    public Pair(V1 obj1, V2 obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }
    public V1 getFirst() {
        return this.obj1;
    }

    public V2 getSecond() {
        return this.obj2;
    }

    public void setFirst(V1 obj1) {
        this.obj1 = obj1;
    }

    public void setSecond(V2 obj2) {
        this.obj2 = obj2;
    }

    public String toString() {
        return Pair.class.getName() + "@" + Integer.toHexString(this.hashCode()) + " [" + this.obj1.toString() + ", " + this.obj2.toString() + "]";
    }
}

