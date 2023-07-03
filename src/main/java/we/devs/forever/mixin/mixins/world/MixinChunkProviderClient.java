/*
 * Decompiled with CFR 0.151.
 *
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  net.minecraft.client.multiplayer.ChunkProviderClient
 *  net.minecraft.world.chunk.Chunk
 */
package we.devs.forever.mixin.mixins.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import we.devs.forever.api.event.events.world.UnloadChunkEvent;
import we.devs.forever.main.ForeverClient;

@Mixin(value = {ChunkProviderClient.class})
public abstract class MixinChunkProviderClient {
    @Redirect(method = {"unloadChunk"}, at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;remove(J)Ljava/lang/Object;", remap = false))
    private Object removeHook(Long2ObjectMap<Chunk> loadedChunks, long l) {
        Chunk chunk = (Chunk) loadedChunks.remove(l);
        if (chunk != null) {
            ForeverClient.EVENT_BUS.post(new UnloadChunkEvent(chunk));
        }
        return chunk;
    }
}

