/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.chunk.Chunk
 */
package we.devs.forever.mixin.mixins.world;

import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import we.devs.forever.api.util.client.CollectionUtil;
import we.devs.forever.mixin.ducks.IChunk;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(value={Chunk.class})
public abstract class MixinChunk
implements IChunk {
    private final Deque<Runnable> postHoleCompilationTasks = new ArrayDeque<Runnable>();
    private boolean compilingHoles = false;
    private int holeVersion;

    @Override
    public void setCompilingHoles(boolean compilingHoles) {
        this.compilingHoles = compilingHoles;
        if (!compilingHoles) {
            CollectionUtil.emptyQueue(this.postHoleCompilationTasks);
        }
    }

    @Override
    public boolean isCompilingHoles() {
        return this.compilingHoles;
    }

    @Override
    public void addHoleTask(Runnable task) {
        if (this.isCompilingHoles()) {
            this.postHoleCompilationTasks.add(task);
        } else {
            task.run();
        }
    }

    @Override
    public int getHoleVersion() {
        return this.holeVersion;
    }

    @Override
    public void setHoleVersion(int version) {
        this.holeVersion = version;
    }
}

