package we.devs.forever.mixin.mixins.render;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.api.util.render.ColorUtil;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.impl.render.Chams;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static we.devs.forever.api.util.Util.mc;

@Mixin(value={RenderLivingBase.class})
public abstract class MixinRenderLivingBase
        extends Render {
    private float prevRenderYawOffset;
    private float renderYawOffset;
    private float prevRotationYawHead;
    private float rotationYawHead;
    private float prevRotationPitch;
    private float rotationPitch;

    protected MixinRenderLivingBase(RenderManager renderManager) {
        super(renderManager);
    }


    @Inject(method={"renderModel"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V", shift=At.Shift.BEFORE)}, cancellable=true)
    private void renderModelHook(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo info) {
        RenderLivingBase<?> renderLiving = RenderLivingBase.class.cast(this);
        ModelBase model = renderLiving.getMainModel();
        if (Chams.getInstance().isEnabled() && entity instanceof EntityPlayer) {
            info.cancel();

            boolean isFriend = Client.friendManager.isFriend(entity.getName());

            float newLimbSwing = Chams.noInterp.getValue() ? 0.0f : limbSwing;
            float newLimbSwingAmount = Chams.noInterp.getValue() ? 0.0f : limbSwingAmount;

            //Prevent it from rendering the chams on ourselves if the Self setting is disabled
            if (!Chams.self.getValue() && entity instanceof EntityPlayerSP) {
                model.render(entity, limbSwing, limbSwingAmount, ageInTicks, 0, 0, scale);
                return;
            }

            //Troll
            if (Chams.sneak.getValue()) {
                entity.setSneaking(true);
            }

            //Model
            if (Chams.model.getValue() == Chams.Model.Vanilla) {
                model.render(entity, newLimbSwing, newLimbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

            } else if (Chams.model.getValue() == Chams.Model.XQZ) {

                glEnable(GL_POLYGON_OFFSET_FILL);
                GlStateManager.enablePolygonOffset();
                glPolygonOffset(1.0f, -1000000);


                Color color = isFriend ?
                        ColorUtil.changeAlpha(Client.friendManager.friendColor, Chams.modelColor.getColor().getAlpha())
                        : Chams.modelColor.getColor();
                RenderUtil.glColor(color);


                model.render(entity, newLimbSwing, newLimbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

                glDisable(GL_POLYGON_OFFSET_FILL);
                GlStateManager.disablePolygonOffset();
                glPolygonOffset(1.0f, 1000000);
            }

            //Wireframe
            if (Chams.wireframe.getValue()) {
                Color color = isFriend ?
                        ColorUtil.changeAlpha(Client.friendManager.friendColor, Chams.lineColor.getColor().getAlpha())
                        : Chams.lineColor.getColor();

                glPushMatrix();
                glPushAttrib(GL_ALL_ATTRIB_BITS);

                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                glDisable(GL_TEXTURE_2D);
                glDisable(GL_LIGHTING);
                glDisable(GL_DEPTH_TEST);
                glEnable(GL_LINE_SMOOTH);
                glEnable(GL_BLEND);

                GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                RenderUtil.glColor(color);

                GlStateManager.glLineWidth(Chams.lineWidth.getValue());

                model.render(entity, newLimbSwing, newLimbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

                glPopAttrib();
                glPopMatrix();
            }

            //Fill
            if (Chams.fill.getValue()) {

                Color color = isFriend ?
                        ColorUtil.changeAlpha(Client.friendManager.friendColor, Chams.color.getColor().getAlpha())
                        : Chams.color.getColor();


                glPushAttrib(GL_ALL_ATTRIB_BITS);
                glDisable(GL_ALPHA_TEST);
                glDisable(GL_TEXTURE_2D);
                glDisable(GL_LIGHTING);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                glLineWidth(1.5f);

                glEnable(GL_STENCIL_TEST);

                if (Chams.xqz.getValue()) {
                    glDisable(GL_DEPTH_TEST);
                    glDepthMask(false);
                }

                glEnable(GL_POLYGON_OFFSET_LINE);
                RenderUtil.glColor(color);

                model.render(entity, newLimbSwing, newLimbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

                if (Chams.xqz.getValue()) {
                    glEnable(GL_DEPTH_TEST);
                    glDepthMask(true);
                }

                glEnable(GL_BLEND);
                glEnable(GL_LIGHTING);
                glEnable(GL_TEXTURE_2D);
                glEnable(GL_ALPHA_TEST);
                glPopAttrib();

            }

            //Glint
            if (Chams.glint.getValue()) {

                Color color = isFriend ?
                        ColorUtil.changeAlpha(Client.friendManager.friendColor, Chams.glintColor.getColor().getAlpha())
                        : Chams.glintColor.getColor();
                glPushMatrix();
                glPushAttrib(GL_ALL_ATTRIB_BITS);
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                glDisable(GL_LIGHTING);

                glDepthRange(0, 0.1);
                                //11
                glEnable(GL_BLEND);

                RenderUtil.glColor(color);

                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);

                float f = (float) entity.ticksExisted + mc.getRenderPartialTicks();

                mc.getRenderManager().renderEngine.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));

                for (int i = 0; i < 2; ++i) {
                    GlStateManager.matrixMode(GL_TEXTURE);

                    GlStateManager.loadIdentity();

                    glScalef(1.0f, 1.0f, 1.0f);
                    GlStateManager.rotate(30.0f - i * 60.0f, 0.0f, 0.0f, 1.0f);
                    GlStateManager.translate(0.0F, f * (0.001F + (float) i * 0.003F) * 20.0F, 0.0F);

                    GlStateManager.matrixMode(GL_MODELVIEW);

                    model.render(entity, newLimbSwing, newLimbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                }

                GlStateManager.matrixMode(GL_TEXTURE);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(GL_MODELVIEW);

                glDisable(GL_BLEND);

                glDepthRange(0, 1);

                glEnable(GL_LIGHTING);

                glPopAttrib();
                glPopMatrix();
            }
        }
    }

}