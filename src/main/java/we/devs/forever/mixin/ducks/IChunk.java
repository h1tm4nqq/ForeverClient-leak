/*
 * Decompiled with CFR 0.151.
 */
package we.devs.forever.mixin.ducks;

public interface IChunk {
    public boolean isCompilingHoles();

    public void setCompilingHoles(boolean var1);

    public void addHoleTask(Runnable var1);

    public int getHoleVersion();

    public void setHoleVersion(int var1);
}

