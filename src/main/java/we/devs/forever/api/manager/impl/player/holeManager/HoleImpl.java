/*
 * Decompiled with CFR 0.151.
 */
package we.devs.forever.api.manager.impl.player.holeManager;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.util.Util;
import we.devs.forever.mixin.ducks.IChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class HoleImpl
implements Hole, Util {
    private final IChunk chunk;
    private final int version;
    private boolean valid = true;
    private final int x;
    private final int y;
    private final int z;
    private final int maxX;
    private final int maxZ;
    private final boolean _2x1;
    private final boolean _2x2;
    private final boolean safe;

    public HoleImpl(IChunk chunk, int x, int y, int z, int maxX, int maxZ, boolean is2x1, boolean is2x2, boolean safe) {
        this.chunk = chunk;
        this.version = chunk.getHoleVersion();
        this.x = x;
        this.y = y;
        this.z = z;
        this.maxX = maxX;
        this.maxZ = maxZ;
        this._2x1 = is2x1;
        this._2x2 = is2x2;
        this.safe = checkHole(safe);
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    public int getMaxX() {
        return this.maxX;
    }

    @Override
    public int getMaxZ() {
        return this.maxZ;
    }

    @Override
    public boolean isSafe() {
        return this.safe;
    }

    @Override
    public boolean is2x1() {
        return this._2x1;
    }

    @Override
    public boolean is2x2() {
        return this._2x2;
    }

    @Override
    public void invalidate() {
        this.valid = false;
    }

    @Override
    public boolean isValid() {
        return this.valid && this.chunk.getHoleVersion() == this.version;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HoleImpl)) {
            return false;
        }
        HoleImpl hole = (HoleImpl)o;
        return this.isValid() == hole.isValid() && this.getX() == hole.getX() && this.getY() == hole.getY() && this.getZ() == hole.getZ() && this.getMaxX() == hole.getMaxX() && this.getMaxZ() == hole.getMaxZ() && this.is2x1() == hole.is2x1() && this.is2x2() == hole.is2x2() && this.isSafe() == hole.isSafe();
    }

    public int hashCode() {
        return Objects.hash(this.isValid(), this.getX(), this.getY(), this.getZ(), this.getMaxX(), this.getMaxZ(), this.is2x1(), this.is2x2(), this.isSafe());
    }

    public String toString() {
        return "HoleImpl{valid=" + this.valid + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", maxX=" + this.maxX + ", maxZ=" + this.maxZ + ", _2x1=" + this._2x1 + ", _2x2=" + this._2x2 + ", safe=" + this.safe + '}';
    }

    public boolean checkHole(boolean safe) {
        AtomicBoolean isSafe = new AtomicBoolean(true);
        if (is2x2()) {
            BlockPos pos = new BlockPos(x, y, z);
            List<BlockPos> checkPoses = new ArrayList<>();
            checkPoses.add(pos);
            checkPoses.add(new BlockPos(pos.north()));
            checkPoses.add(new BlockPos(pos.west()));
            checkPoses.add(new BlockPos(maxX,y, maxZ));
            checkPoses.forEach(pos1 -> {
                if (isSafe.get()) isSafe.set(checkSafe(pos1));
            });
            return isSafe.get();
        }
        if (is2x1()) {
            List<BlockPos> checkPoses = new ArrayList<>();
            checkPoses.add(new BlockPos(x, y, z));
            checkPoses.add(new BlockPos(maxX,y, maxZ));
            checkPoses.forEach(pos1 -> {
                if (isSafe.get()) isSafe.set(checkSafe(pos1));
            });
            return isSafe.get();
        }

        return safe;
    }

    private boolean checkSafe(BlockPos pos) {
        AtomicBoolean isSafe = new AtomicBoolean(true);
        List<BlockPos> poses = new ArrayList<>();
        poses.add(pos.down());
        poses.add(pos.north());
        poses.add(pos.south());
        poses.add(pos.east());
        poses.add(pos.west());
        poses.forEach(pos1 -> {
            IBlockState state = mc.world.getBlockState(pos1);
            if (!state.getBlock().equals(Blocks.AIR) || !state.getBlock().equals(Blocks.BEDROCK)) {
                isSafe.set(false);
            }
        });
        return isSafe.get();
    }
}

