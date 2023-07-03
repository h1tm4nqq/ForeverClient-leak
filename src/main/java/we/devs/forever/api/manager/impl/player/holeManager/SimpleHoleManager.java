/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package we.devs.forever.api.manager.impl.player.holeManager;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleHoleManager
implements HoleManager {
    private final Map<BlockPos, Hole> holes;
    private final List<Hole> _1x1_safe;
    private final List<Hole> _1x1_unsafe;
    private final List<Hole> _2x1;
    private final List<Hole> _2x2;

    public SimpleHoleManager() {
        this(new HashMap<BlockPos, Hole>(), new ArrayList<Hole>(), new ArrayList<Hole>(), new ArrayList<Hole>(), new ArrayList<Hole>());
    }

    public SimpleHoleManager(Map<BlockPos, Hole> holes, List<Hole> _1x1_safe, List<Hole> _1x1_unsafe, List<Hole> _2x1, List<Hole> _2x2) {
        this.holes = holes;
        this._1x1_safe = _1x1_safe;
        this._1x1_unsafe = _1x1_unsafe;
        this._2x1 = _2x1;
        this._2x2 = _2x2;
    }

    @Override
    public Map<BlockPos, Hole> getHoles() {
        return this.holes;
    }

    @Override
    public List<Hole> get1x1() {
        return this._1x1_safe;
    }

    @Override
    public List<Hole> get1x1Unsafe() {
        return this._1x1_unsafe;
    }

    @Override
    public List<Hole> get2x1() {
        return this._2x1;
    }

    @Override
    public List<Hole> get2x2() {
        return this._2x2;
    }

    @Override
    public List<Hole> getHolesList() {
        return null;
    }
}

