/*
 * Decompiled with CFR 0.151.
 */
package we.devs.forever.api.manager.impl.player.holeManager;

public interface InvalidationConfig {
    public boolean isUsingInvalidationHoleManager();

    public boolean shouldCalcChunksAsnyc();

    public boolean limitChunkThreads();

    public int getHeight();

    public int getSortTime();

    public int getRemoveTime();
}

