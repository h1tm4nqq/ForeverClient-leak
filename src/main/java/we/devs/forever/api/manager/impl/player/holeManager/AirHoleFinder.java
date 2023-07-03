//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "D:\Rat Checker\1.12 stable mappings"!

/*
 * Decompiled with CFR 0.151.
 *
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package we.devs.forever.api.manager.impl.player.holeManager;

import net.minecraft.util.math.BlockPos;
import we.devs.forever.mixin.ducks.IChunk;

public class AirHoleFinder
extends HoleFinder {
    private IChunk chunk;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int minZ;
    private int maxZ;

    public AirHoleFinder(HoleManager h) {
        super(h, h.getHoles(), h.get1x1(), h.get1x1Unsafe(), h.get2x1(), h.get2x2(), new MutPos(), null, 0, 0, 0, 0, 0, 0);
    }

    @Override
    public void calcHoles() {
        for (int x = this.getMinX(); x < this.getMaxX(); ++x) {
            for (int z = this.getMinZ(); z < this.getMaxZ(); ++z) {
                for (int y = this.getMinY(); y <= this.getMaxY(); ++y) {
                    Hole hole = (Hole)this.map.get(this.pos.setPos(x, y, z));
                    if (hole != null && hole.isValid()) continue;
                    this.calcHole();
                }
            }
        }
    }

    public void setPos(BlockPos pos) {
        this.setMaxX(pos.getX() + 1);
        this.setMinX(pos.getX() - 1);
        this.setMaxY(pos.getY());
        this.setMinY(pos.getY() - 2);
        this.setMaxZ(pos.getZ() + 1);
        this.setMinZ(pos.getZ() - 1);
    }

    @Override
    public IChunk getChunk() {
        return this.chunk;
    }

    public void setChunk(IChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public int getMinX() {
        return this.minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    @Override
    public int getMaxX() {
        return this.maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    @Override
    public int getMinY() {
        return this.minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    @Override
    public int getMaxY() {
        return this.maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    @Override
    public int getMinZ() {
        return this.minZ;
    }

    public void setMinZ(int minZ) {
        this.minZ = minZ;
    }

    @Override
    public int getMaxZ() {
        return this.maxZ;
    }

    public void setMaxZ(int maxZ) {
        this.maxZ = maxZ;
    }
}

