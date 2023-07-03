/*
 * Decompiled with CFR 0.151.
 */
package we.devs.forever.api.manager.impl.player.holeManager.thread.holes;

public interface HoleObserver
extends Comparable<HoleObserver> {
    /**
     * @return the minimum range in which holes should
     *         be checked while this HoleObserver is registered.
     */
    double getRange();

    /**
     * @return the Amount of safe holes that should be calculated.
     */
    int getSafeHoles();

    /**
     * @return the Amount of unsafe holes that should be calculated.
     */
    int getUnsafeHoles();

    /**
     * @return the Amount of 2x1 holes that should be calculated.
     */
    int get2x1Holes();

    /**
     * @return the Amount of 2x2 holes that should be calculated.
     */
    int get2x2Holes();

    default boolean isThisHoleObserverActive() {
        return true;
    }

    @Override
    default int compareTo(HoleObserver o)
    {
        return Double.compare(this.getRange(), o.getRange());
    }
}

