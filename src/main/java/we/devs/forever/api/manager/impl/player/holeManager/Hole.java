//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "D:\Rat Checker\1.12 stable mappings"!

/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package we.devs.forever.api.manager.impl.player.holeManager;

import net.minecraft.util.math.BlockPos;

public interface Hole {
    public int getX();

    public int getY();

    public int getZ();

    public int getMaxX();

    public int getMaxZ();

    public boolean isSafe();

    public boolean is2x1();

    public boolean is2x2();

    public void invalidate();

    public boolean isValid();

    default public boolean isAirPart(BlockPos pos) {
        return this.isAirPart(pos.getX(), pos.getY(), pos.getZ());
    }

    default public boolean isAirPart(int x, int y, int z) {
        return x >= this.getX() && y >= this.getY() && z >= this.getZ() && x < this.getMaxX() && y < this.getY() + 2 && z < this.getMaxZ();
    }

    default public double getDistanceSq(double x, double y, double z) {
        double xDiff = (double)this.getX() - x;
        double yDiff = (double)this.getY() - y;
        double zDiff = (double)this.getZ() - z;
        return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
    }

    default public boolean contains(double x, double y, double z) {
        return x > (double)this.getX() && x < (double)this.getMaxX() && y >= (double)this.getY() && y < (double)(this.getY() + 1) && z > (double)this.getZ() && z < (double)this.getMaxZ();
    }
}

