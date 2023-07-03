package we.devs.forever.mixin.mixins.render;

import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.impl.exploit.NoEntityTrace;
import we.devs.forever.client.modules.impl.render.CameraClip;
import we.devs.forever.client.modules.impl.render.NoRender;
import we.devs.forever.client.modules.impl.render.Sky;
import we.devs.forever.client.modules.impl.render.shader.Shader;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = EntityRenderer.class, priority = Integer.MAX_VALUE)
public abstract
class MixinEntityRenderer {
    @Shadow
    public ItemStack itemActivationItem;
    @Shadow
    @Final
    public Minecraft mc;
    @Shadow
    public boolean debugView;
    @Shadow
    public float farPlaneDistance;
    @Final
    @Shadow
    public ItemRenderer itemRenderer;
    @Shadow
    @Final
    private int[] lightmapColors;
    private boolean injection = true;

    @Shadow
    public abstract void getMouseOver(float partialTicks);

    @Inject(method = "renderItemActivation", at = @At("HEAD"), cancellable = true)
    public void renderItemActivationHook(CallbackInfo info) {
        if (this.itemActivationItem != null && (NoRender.getInstance().isEnabled() && NoRender.getInstance().totemPops.getValue() && this.itemActivationItem.getItem() == Items.TOTEM_OF_UNDYING)) {
            info.cancel();
        }
    }

    @Inject(method = "updateLightmap", at = @At("HEAD"), cancellable = true)
    private void updateLightmap(float partialTicks, CallbackInfo info) {
        if (NoRender.getInstance().isEnabled() && (NoRender.getInstance().skylight.getValue() == NoRender.Skylight.Entity || NoRender.getInstance().skylight.getValue() == NoRender.Skylight.All)) {
            info.cancel();
        }
    }

    @Inject(method = {"updateLightmap"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;updateDynamicTexture()V", shift = At.Shift.BEFORE)})
    private void updateTextureHook(final float partialTicks, final CallbackInfo ci) {
        try {
            if (Sky.ambience.getValue()) {
                for (int i = 0; i < this.lightmapColors.length; ++i) {
                    final Color ambientColor = Sky.color.getColor();
                    final int alpha = ambientColor.getAlpha();
                    final float modifier = alpha / 255.0f;
                    final int color = this.lightmapColors[i];
                    final int[] bgr = this.toRGBAArray(color);
                    final Vector3f values = new Vector3f(bgr[2] / 255.0f, bgr[1] / 255.0f, bgr[0] / 255.0f);
                    final Vector3f newValues = new Vector3f(ambientColor.getRed() / 255.0f, ambientColor.getGreen() / 255.0f, ambientColor.getBlue() / 255.0f);
                    final Vector3f finalValues = this.mix(values, newValues, modifier);
                    final int red = (int) (finalValues.x * 255.0f);
                    final int green = (int) (finalValues.y * 255.0f);
                    final int blue = (int) (finalValues.z * 255.0f);
                    this.lightmapColors[i] = (0xFF000000 | red << 16 | green << 8 | blue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int[] toRGBAArray(final int colorBuffer) {
        return new int[]{colorBuffer >> 16 & 0xFF, colorBuffer >> 8 & 0xFF, colorBuffer & 0xFF};
    }

    private Vector3f mix(final Vector3f first, final Vector3f second, final float factor) {
        return new Vector3f(first.x * (1.0f - factor) + second.x * factor, first.y * (1.0f - factor) + second.y * factor, first.z * (1.0f - factor) + first.z * factor);
    }

    @Redirect(method = {"getMouseOver"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcludingHook(WorldClient worldClient, @Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
        if (NoEntityTrace.getInstance().isEnabled() && NoEntityTrace.getInstance().noTrace) {
            return new ArrayList<>();
        }
        return worldClient.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    //TODO: WTF
    @Inject(method = "getMouseOver(F)V", at = @At(value = "HEAD"), cancellable = true)
    public void getMouseOverHook(float partialTicks, CallbackInfo info) {
        if (injection) {
            info.cancel();
            injection = false;
            try {
                this.getMouseOver(partialTicks);
            } catch (Exception e) {
                e.printStackTrace();
            }
            injection = true;
        }
    }

    @Redirect(method = "setupCameraTransform", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;prevTimeInPortal:F"))
    public float prevTimeInPortalHook(EntityPlayerSP entityPlayerSP) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().nausea.getValue()) {
            return -3.4028235E38f;
        }
        return entityPlayerSP.prevTimeInPortal;
    }

    //  @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;clear(I)V", ordinal = 1, shift = At.Shift.BEFORE))
    @Inject(
            method = "renderWorldPass",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GlStateManager;clear(I)V",
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            )
    )
    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (Display.isActive() || Display.isVisible()) {
            Render3DEvent render3dEvent = new Render3DEvent(partialTicks);
            Client.moduleManager.onRender3D(render3dEvent);
            GlStateManager.resetColor();
            GlStateManager.color(1F, 1F, 1F, 1F);
        }
    }


    @Inject(method = "renderHand", at = @At("HEAD"), cancellable = true)
    public void renderHandMain(float partialTicks, int pass, CallbackInfo ci) {
        Shader module = Shader.INSTANCE;
        if (module.isEnabled() && !Shader.items.getValue().equals(Shader.Mode.None)) {
            ci.cancel();
            Minecraft mc = Minecraft.getMinecraft();
            if (!Shader.itemsCancel.getValue()) {
                doRenderHand(partialTicks, pass, mc);
            }
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableAlpha();
            if(Shader.items.getValue().equals(Shader.Mode.Fill) || Shader.items.getValue().equals(Shader.Mode.Both)){
                module.runPreFill();
                doRenderHand(partialTicks, pass, mc);
            }

            if(Shader.items.getValue().equals(Shader.Mode.OutLine) || Shader.items.getValue().equals(Shader.Mode.Both)) {
                module.runPreGlow();
                doRenderHand(partialTicks, pass, mc);
            }
            if(Shader.items.getValue().equals(Shader.Mode.Fill) || Shader.items.getValue().equals(Shader.Mode.Both))
                module.runPostFill();

            if(Shader.items.getValue().equals(Shader.Mode.OutLine) || Shader.items.getValue().equals(Shader.Mode.Both))
                module.runPostGlow();

            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }

    @Inject(method = "setupFog", at = @At(value = "HEAD"), cancellable = true)
    public void setupFogHook(int startCoords, float partialTicks, CallbackInfo info) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().fog.getValue() == NoRender.Fog.NoFog) {
            info.cancel();
        }
    }

    @Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockStateAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/state/IBlockState;"))
    public IBlockState getBlockStateAtEntityViewpointHook(World worldIn, Entity entityIn, float p_186703_2_) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().fog.getValue() == NoRender.Fog.Air) {
            return Blocks.AIR.defaultBlockState;
        }
        return ActiveRenderInfo.getBlockStateAtEntityViewpoint(worldIn, entityIn, p_186703_2_);
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffectHook(float ticks, CallbackInfo info) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().hurtcam.getValue()) {
            info.cancel();
        }
    }
    @Shadow
    protected abstract float getFOVModifier(final float p0, final boolean p1);

    @Shadow
    protected abstract void hurtCameraEffect(final float p0);

    @Shadow
    protected abstract void applyBobbing(final float p0);

    @Shadow
    public abstract void enableLightmap();

    @Shadow
    public abstract void disableLightmap();
    void doRenderHand(float partialTicks, int pass, Minecraft mc) {
        GlStateManager.pushMatrix();
        if (!this.debugView) {
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            if (mc.gameSettings.anaglyph) {
                GlStateManager.translate((float) (-(pass * 2 - 1)) * 0.07F, 0.0F, 0.0F);
            }
            Project.gluPerspective(this.getFOVModifier(partialTicks, false), (float) mc.displayWidth / (float) mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            if (mc.gameSettings.anaglyph) {
                GlStateManager.translate((float) (pass * 2 - 1) * 0.1F, 0.0F, 0.0F);
            }
            this.hurtCameraEffect(partialTicks);
            if (mc.gameSettings.viewBobbing) {
                this.applyBobbing(partialTicks);
            }
            boolean flag = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();

            if (!net.minecraftforge.client.ForgeHooksClient.renderFirstPersonHand(mc.renderGlobal, partialTicks, pass))
                if (mc.gameSettings.thirdPersonView == 0 && !flag && !mc.gameSettings.hideGUI && !mc.playerController.isSpectator()) {
                    this.enableLightmap();
                    this.itemRenderer.renderItemInFirstPerson(partialTicks);
                    this.disableLightmap();
                }

            GlStateManager.popMatrix();

            if (mc.gameSettings.thirdPersonView == 0 && !flag) {
                this.itemRenderer.renderOverlays(partialTicks);
                this.hurtCameraEffect(partialTicks);
            }

            if (mc.gameSettings.viewBobbing) {
                this.applyBobbing(partialTicks);
            }
        }
    }
    @SuppressWarnings("all")
    @ModifyVariable(method = "orientCamera", ordinal = 3, at = @At(value = "STORE", ordinal = 0), require = 1)
    public double changeCameraDistanceHook(double range) {
        return (CameraClip.getInstance().isEnabled() && CameraClip.getInstance().extend.getValue()) ? CameraClip.getInstance().distance.getValue() : range;
    }
    @SuppressWarnings("all")
    @ModifyVariable(method = "orientCamera", ordinal = 7, at = @At(value = "STORE", ordinal = 0), require = 1)
    public double orientCameraHook(double range) {
        return (CameraClip.getInstance().isEnabled() && CameraClip.getInstance().extend.getValue()) ? CameraClip.getInstance().distance.getValue() : (CameraClip.getInstance().isEnabled() && !CameraClip.getInstance().extend.getValue()) ? 4.0 : range;
    }
}
