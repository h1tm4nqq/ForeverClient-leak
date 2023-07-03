package we.devs.forever.mixin.mixins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.api.event.events.player.BlockBreakingEvent;
import we.devs.forever.api.event.events.render.FogEvent;
import we.devs.forever.main.ForeverClient;

import java.util.Set;


@Mixin(RenderGlobal.class)
public abstract
class MixinRenderGlobal {
    @Shadow private Set<RenderChunk> chunksToUpdate;

    /*
            @Redirect(method = {"setupTerrain"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ChunkRenderContainer;initialize(DDD)V"))
            public void initializeHook(final ChunkRenderContainer chunkRenderContainer, final double viewEntityXIn, final double viewEntityYIn, final double viewEntityZIn) {
             if (InstantSprint.getInstance().isEnabled() && InstantSprint.getInstance().noShake.getValue() && InstantSprint.getInstance().mode.getValue() != InstantSprint.Mode.INSTANT && InstantSprint.getInstance().antiShake) {
                    y = InstantSprint.getInstance().startY;
                }


                chunkRenderContainer.initialize(viewEntityXIn, y, viewEntityZIn);
            }

            @Redirect(method = {"renderEntities"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;setRenderPosition(DDD)V"))
            public void setRenderPositionHook(final RenderManager renderManager, final double renderPosXIn, final double renderPosYIn, final double renderPosZIn) {
                double y = renderPosYIn;
                if (InstantSprint.getInstance().isEnabled() && InstantSprint.getInstance().noShake.getValue() && InstantSprint.getInstance().mode.getValue() != InstantSprint.Mode.INSTANT && InstantSprint.getInstance().antiShake) {
                    y = InstantSprint.getInstance().startY;
                }
                renderManager.setRenderPosition(renderPosXIn, TileEntityRendererDispatcher.staticPlayerY = y, renderPosZIn);
            }

            @Redirect(method = {"drawSelectionBox"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/AxisAlignedBB;offset(DDD)Lnet/minecraft/util/math/AxisAlignedBB;"))
            public AxisAlignedBB offsetHook(final AxisAlignedBB axisAlignedBB, final double x, final double y, final double z) {
                if (InstantSprint.getInstance().isEnabled() && InstantSprint.getInstance().noShake.getValue() && InstantSprint.getInstance().mode.getValue() != InstantSprint.Mode.INSTANT) {
                    InstantSprint.getInstance();
                }
                return axisAlignedBB.offset(x, y, z);
            }
        */

    @Inject(method = {"sendBlockBreakProgress"}, at = {@At("HEAD")})
    public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress, final CallbackInfo ci) {
        final BlockBreakingEvent event = new BlockBreakingEvent(pos, breakerId, progress);
        ForeverClient.EVENT_BUS.post(event);
    }

    @Redirect(method = "renderSky(FI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getSkyColor(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d getSkyColorRedirect(WorldClient worldClient, Entity entityIn, float partialTicks) {
        Vec3d sky = Minecraft.getMinecraft().world.getSkyColor(entityIn, partialTicks);
        FogEvent.Color event = new FogEvent.Color((float) sky.x, (float) sky.y, (float) sky.z);
        ForeverClient.EVENT_BUS.post(event);
        return new Vec3d(event.getR(), event.getG(), event.getB());
    }


}


