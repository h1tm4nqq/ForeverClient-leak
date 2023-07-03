package we.devs.forever.mixin.mixins.world;

import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.BlockSnapshot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import we.devs.forever.api.event.events.entity.EntityWorldEvent;
import we.devs.forever.api.event.events.player.PushEvent;
import we.devs.forever.api.event.events.render.GetWorldTimeEvent;
import we.devs.forever.api.event.events.world.BlockStateChangeEvent;
import we.devs.forever.client.modules.impl.render.NoRender;
import we.devs.forever.main.ForeverClient;
import we.devs.forever.mixin.ducks.IChunk;

import java.util.List;

@Mixin(World.class)
public
class MixinWorld {
    @Shadow
    @Final
    public boolean isRemote;

    @Shadow
    @Final
    @Mutable
    public List<Entity> loadedEntityList;
    @Shadow
    @Final
    @Mutable
    public List<Entity> playerEntities;
    @Redirect(method = "getEntitiesWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getEntitiesOfTypeWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lcom/google/common/base/Predicate;)V"))
    public <T extends Entity> void getEntitiesOfTypeWithinAABBHook(Chunk chunk, Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> filter) {
        try {
            chunk.getEntitiesOfTypeWithinAABB(entityClass, aabb, listToFill, filter);
        } catch (Exception ignored) {
        }
    }
    @Inject(method = {"getWorldTime"}, at = {@At("HEAD")}, cancellable = true)
    public void onGetWorldTime(CallbackInfoReturnable<Long> cir) {
        GetWorldTimeEvent event = ForeverClient.EVENT_BUS.post(new GetWorldTimeEvent());
        if (event.isCanceled()) cir.setReturnValue(event.getWorldTime());
    }
    @Inject(method = "spawnEntity", at = @At("RETURN"), cancellable = true)
    public void onSpawnEntity(Entity entity, CallbackInfoReturnable<Boolean> info) {
        EntityWorldEvent.EntitySpawnEvent entitySpawnEvent = new EntityWorldEvent.EntitySpawnEvent(entity);
        ForeverClient.EVENT_BUS.post(entitySpawnEvent);

        if (entitySpawnEvent.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "removeEntity", at = @At("HEAD"), cancellable = true)
    public void onRemoveEntity(Entity entity, CallbackInfo info) {
        EntityWorldEvent.EntityRemoveEvent entityRemoveEvent = new EntityWorldEvent.EntityRemoveEvent(entity);
        ForeverClient.EVENT_BUS.post(entityRemoveEvent);

        if (entityRemoveEvent.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "removeEntityDangerously", at = @At("HEAD"), cancellable = true)
    public void onRemoveEntityDangerously(Entity entity, CallbackInfo info) {
        EntityWorldEvent.EntityRemoveEvent entityRemoveEvent = new EntityWorldEvent.EntityRemoveEvent(entity);
        ForeverClient.EVENT_BUS.post(entityRemoveEvent);

        if (entityRemoveEvent.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "updateEntity", at = @At("HEAD"), cancellable = true)
    public void onUpdateEntity(Entity entity, CallbackInfo info) {
        EntityWorldEvent.EntityUpdateEvent entityUpdateEvent = new EntityWorldEvent.EntityUpdateEvent(entity);
        ForeverClient.EVENT_BUS.post(entityUpdateEvent);

        if (entityUpdateEvent.isCanceled()) {
            info.cancel();
        }
    }


    @Inject(method = "onEntityAdded", at = @At("HEAD"))
    private void onEntityAdded(Entity entityIn, CallbackInfo ci) {

    }

    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    private void updateLightmapHook(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        if (lightType == EnumSkyBlock.SKY && NoRender.getInstance().isEnabled() && (NoRender.getInstance().skylight.getValue() == NoRender.Skylight.World || NoRender.getInstance().skylight.getValue() == NoRender.Skylight.All)) {
            info.setReturnValue(true);
            info.cancel();
        }
    }

    @Inject(method={"setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"}, at={@At(value="INVOKE", target="Lnet/minecraft/block/state/IBlockState;getLightOpacity(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)I", shift=At.Shift.BEFORE, ordinal=1, remap=false)}, locals= LocalCapture.CAPTURE_FAILHARD)
    private void onSetBlockState(BlockPos pos, IBlockState newState, int flags, CallbackInfoReturnable<Boolean> cir, Chunk chunk, BlockSnapshot blockSnapshot, IBlockState oldState, int oldLight, int oldOpacity, IBlockState iblockstate) {
        if (this.isRemote) {
            BlockStateChangeEvent event = new BlockStateChangeEvent(pos, newState, (IChunk)chunk);
            ForeverClient.EVENT_BUS.post(event);
        }
    }

    //TODO: Find a better Injection Point that allows us to multiply the vec with Velocity h and vspeed instead, this should work for no push at all tho(We can also just AT the method but yeh)
    @Redirect(method = "handleMaterialAcceleration", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isPushedByWater()Z"))
    public boolean isPushedbyWaterHook(Entity entity) {
        PushEvent event = new PushEvent(2, entity);
        ForeverClient.EVENT_BUS.post(event);
        return entity.isPushedByWater() && !event.isCanceled();
    }
}

