/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.WorldClient
 *  net.minecraft.entity.Entity
 */
package we.devs.forever.mixin.mixins.world;

import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.api.event.events.network.WorldClientEvent;
import we.devs.forever.main.ForeverClient;

@Mixin(value={WorldClient.class})
public abstract class MixinWorldClient {
//    private static final ModuleCache<NoRender> NO_RENDER = Caches.getModule(NoRender.class);

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    public void constructorHook(CallbackInfo callbackInfo) {
        ForeverClient.EVENT_BUS.post(new WorldClientEvent.Load((WorldClient)WorldClient.class.cast(this)));
    }

//    @ModifyVariable(method={"showBarrierParticles(IIIILjava/util/Random;ZLnet/minecraft/util/math/BlockPos$MutableBlockPos;)V"}, at=@At(value="HEAD"))
//    public boolean showBarrierParticlesHook(boolean holdingBarrier) {
//        return NO_RENDER.returnIfPresent(NoRender::showBarriers, false) != false || holdingBarrier;
//    }
//
//    @Inject(method={"onEntityAdded"}, at={@At(value="HEAD")})
//    public void onEntityAdded(Entity entity, CallbackInfo info) {
//        Bus.EVENT_BUS.post(new EntityChunkEvent(Stage.PRE, entity));
//    }
//
//    @Inject(method={"onEntityRemoved"}, at={@At(value="HEAD")})
//    public void onEntityRemoved(Entity entity, CallbackInfo info) {
//        Bus.EVENT_BUS.post(new EntityChunkEvent(Stage.POST, entity));
//    }
}

