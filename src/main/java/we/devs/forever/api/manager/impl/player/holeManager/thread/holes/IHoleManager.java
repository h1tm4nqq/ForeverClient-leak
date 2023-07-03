/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package we.devs.forever.api.manager.impl.player.holeManager.thread.holes;

import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface IHoleManager {
    public void setSafe(List<BlockPos> var1);

    public void setUnsafe(List<BlockPos> var1);

    public void setLongHoles(List<BlockPos> var1);

    public void setBigHoles(List<BlockPos> var1);

    public void setFinished();
}

