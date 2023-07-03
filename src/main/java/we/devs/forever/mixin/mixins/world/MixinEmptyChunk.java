/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.chunk.EmptyChunk
 */
package we.devs.forever.mixin.mixins.world;

import net.minecraft.world.chunk.EmptyChunk;
import org.spongepowered.asm.mixin.Mixin;
import we.devs.forever.mixin.ducks.IChunk;

@Mixin(value={EmptyChunk.class})
public abstract class MixinEmptyChunk
implements IChunk {
    @Override
    public void setCompilingHoles(boolean compilingHoles) {
    }

    @Override
    public boolean isCompilingHoles() {
        return false;
    }

    @Override
    public void addHoleTask(Runnable task) {
    }
}

