//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "D:\Rat Checker\1.12 stable mappings"!

/*
 * Decompiled with CFR 0.151.
 *
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Vec3i
 */
package we.devs.forever.api.manager.impl.player.holeManager;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import we.devs.forever.mixin.ducks.IChunk;

public class BlockHoleFinder
extends HoleFinder {
    private static final Vec3i[] OFFSETS = new Vec3i[]{new Vec3i(-1, 0, -1), new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(-1, 1, -1), new Vec3i(-1, 0, -2), new Vec3i(-2, 0, -1), new Vec3i(-1, 0, 1), new Vec3i(1, 0, -1), new Vec3i(-1, 1, 0), new Vec3i(0, 1, -1), new Vec3i(0, 0, 1), new Vec3i(-2, 0, 0), new Vec3i(1, 0, 0), new Vec3i(0, 0, -2), new Vec3i(0, 1, 0)};
    private IChunk chunk;
    private int x;
    private int y;
    private int z;

    public BlockHoleFinder(HoleManager h) {
        super(h, h.getHoles(), h.get1x1(), h.get1x1Unsafe(), h.get2x1(), h.get2x2(), new MutPos(), null, 0, 0, 0, 0, 0, 0);
    }

    @Override
    public void calcHoles() {
        for (Vec3i off : OFFSETS) {
            Hole hole = (Hole)this.map.get(this.pos.setPos(this.getX() + off.getX(), this.getY() + off.getY(), this.getZ() + off.getZ()));
            if (hole != null && hole.isValid()) continue;
            this.calcHole();
        }
    }

    public void setPos(BlockPos pos) {
        this.setX(pos.getX());
        this.setY(pos.getY());
        this.setZ(pos.getZ());
    }

    @Override
    public IChunk getChunk() {
        return this.chunk;
    }

    public void setChunk(IChunk chunk) {
        this.chunk = chunk;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return this.z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}

