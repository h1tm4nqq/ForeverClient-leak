/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.chunk.Chunk
 */
package we.devs.forever.api.event.events.world;

import net.minecraft.world.chunk.Chunk;

public class UnloadChunkEvent {
    private final Chunk chunk;

    public UnloadChunkEvent(Chunk chunk) {
        this.chunk = chunk;
    }

    public Chunk getChunk() {
        return this.chunk;
    }
}

