/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package we.devs.forever.api.manager.impl.player.holeManager;

import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public interface HoleManager {
    Map<BlockPos, Hole> getHoles();

    List<Hole> get1x1();

    List<Hole> get1x1Unsafe();

    List<Hole> get2x1();

    List<Hole> get2x2();

    default void reset()
    {
        getHoles().clear();
        get1x1().clear();
        get1x1Unsafe().clear();
        get2x1().clear();
        get2x2().clear();
    }


    List<Hole> getHolesList();
}

