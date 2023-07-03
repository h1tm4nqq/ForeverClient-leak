//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "D:\Rat Checker\1.12 stable mappings"!

/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos$MutableBlockPos
 */
package we.devs.forever.api.manager.impl.player.holeManager;

import net.minecraft.util.math.BlockPos;

public class MutPos
extends BlockPos.MutableBlockPos {
    public void setX(int x) {
        this.x = x;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void incrementX(int by) {
        this.x += by;
    }

    public void incrementY(int by) {
        this.y += by;
    }

    public void incrementZ(int by) {
        this.z += by;
    }
}

