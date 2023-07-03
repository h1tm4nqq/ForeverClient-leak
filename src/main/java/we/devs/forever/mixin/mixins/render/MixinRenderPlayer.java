package we.devs.forever.mixin.mixins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.impl.render.HandChams;
import we.devs.forever.client.modules.impl.render.nametags.Nametags;


@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {
    private float prevRenderYawOffset;
    private float renderYawOffset;
    private float prevRotationYawHead;
    private float rotationYawHead;
    private float prevRotationPitch;
    private float rotationPitch;


    @Inject(method = "renderEntityName", at = @At("HEAD"), cancellable = true)
    public void renderEntityNameHook(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info) {
        if (Nametags.getInstance().isEnabled()) {
            info.cancel();
        }
    }

    @Inject(method={"doRender"}, at={@At(value="HEAD")})
    public void doRenderHookHead(AbstractClientPlayer entityLivingBase, double d, double d2, double d3, float f, float f2, CallbackInfo callbackInfo) {
        if (entityLivingBase instanceof EntityPlayerSP) {
            this.prevRenderYawOffset = entityLivingBase.prevRenderYawOffset;
            this.renderYawOffset = entityLivingBase.renderYawOffset;
            this.prevRotationYawHead = entityLivingBase.prevRotationYawHead;
            this.rotationYawHead = entityLivingBase.rotationYawHead;
            this.prevRotationPitch = entityLivingBase.prevRotationPitch;
            this.rotationPitch = entityLivingBase.rotationPitch;
            entityLivingBase.prevRenderYawOffset = Client.renderRotationManager.getPrevRenderYawOffset();
            entityLivingBase.renderYawOffset = Client.renderRotationManager.getRenderYawOffset();
            entityLivingBase.prevRotationYawHead = Client.renderRotationManager.getPrevRotationYawHead();
            entityLivingBase.rotationYawHead = Client.renderRotationManager.getRotationYawHead();
            entityLivingBase.prevRotationPitch = Client.renderRotationManager.getPrevPitch();
            entityLivingBase.rotationPitch = Client.renderRotationManager.getRenderPitch();
        }
    }

    @Inject(method={"doRender"}, at={@At(value="RETURN")})
    public void doRenderHookReturn(AbstractClientPlayer entityLivingBase, double d, double d2, double d3, float f, float f2, CallbackInfo callbackInfo) {
        if (entityLivingBase instanceof EntityPlayerSP) {
            entityLivingBase.prevRenderYawOffset = this.prevRenderYawOffset;
            entityLivingBase.renderYawOffset = this.renderYawOffset;
            entityLivingBase.prevRotationYawHead = this.prevRotationYawHead;
            entityLivingBase.rotationYawHead = this.rotationYawHead;
            entityLivingBase.prevRotationPitch = this.prevRotationPitch;
            entityLivingBase.rotationPitch = this.rotationPitch;
        }
    }

    @Inject(method = {"renderRightArm"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPlayer;swingProgress:F", opcode = 181)}, cancellable = true)
    public void renderRightArmBegin(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (clientPlayer == Minecraft.getMinecraft().player && HandChams.INSTANCE.isEnabled()) {
            GL11.glPushAttrib(1048575);
            GL11.glDisable(3008);
            GL11.glDisable(3553);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glLineWidth(1.5f);
            GL11.glEnable(2960);
            GL11.glEnable(10754);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
            RenderUtil.glColor(HandChams.INSTANCE.color.getColor());
        }
    }

    @Inject(method = {"renderRightArm"}, at = {@At(value = "RETURN")})
    public void renderRightArmReturn(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (clientPlayer == Minecraft.getMinecraft().player && HandChams.INSTANCE.isEnabled()) {
            GL11.glEnable(3042);
            GL11.glEnable(2896);
            GL11.glEnable(3553);
            GL11.glEnable(3008);
            GL11.glPopAttrib();
        }
    }

    @Inject(method = {"renderLeftArm"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPlayer;swingProgress:F", opcode = 181)})
    public void renderLeftArmBegin(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (clientPlayer == Minecraft.getMinecraft().player && HandChams.INSTANCE.isEnabled()) {
            GL11.glPushAttrib(1048575);
            GL11.glDisable(3008);
            GL11.glDisable(3553);
            GL11.glDisable(2896);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glLineWidth(1.5f);
            GL11.glEnable(2960);
            GL11.glEnable(10754);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
            RenderUtil.glColor(HandChams.INSTANCE.color.getColor());
        }
    }

    @Inject(method = {"renderLeftArm"}, at = {@At(value = "RETURN")})
    public void renderLeftArmReturn(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (clientPlayer == Minecraft.getMinecraft().player && HandChams.INSTANCE.isEnabled()) {
            GL11.glEnable(3042);
            GL11.glEnable(2896);
            GL11.glEnable(3553);
            GL11.glEnable(3008);
            GL11.glPopAttrib();
        }
    }
}
