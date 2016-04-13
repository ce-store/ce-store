package com.ibm.ets.ita.ce.store.utilities;

public class Tuple<X, Y> {

    public final X x;
    public final Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    @Override
	public String toString() {
        return "[Tuple] x=" + x + " y=" + y;
    }
}
