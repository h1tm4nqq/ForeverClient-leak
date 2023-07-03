package we.devs.forever.mixin.mixins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEnderCrystal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;
import we.devs.forever.client.modules.impl.render.CrystalModify;

import java.awt.*;

@Mixin(value = {RenderEnderCrystal.class})
public abstract class MixinRenderEnderCrystal {
    private static final ResourceLocation RES_ITEM_GLINT;
    @Final
    @Shadow
    private static ResourceLocation ENDER_CRYSTAL_TEXTURES;

    static {
        RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    }

    @Final
    @Shadow
    private ModelBase modelEnderCrystal;
    @Final
    @Shadow
    private ModelBase modelEnderCrystalNoBase;


    @Redirect(method = {"doRender(Lnet/minecraft/entity/item/EntityEnderCrystal;DDDFF)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"))
    private void render1(ModelBase var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
        if (CrystalModify.INSTANCE.isDisabled()) {
            var1.render(var2, var3, var4, var5, var6, var7, var8);
        }
    }

    @Redirect(method = {"doRender(Lnet/minecraft/entity/item/EntityEnderCrystal;DDDFF)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V", ordinal = 1))
    private void render2(ModelBase var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
        if (CrystalModify.INSTANCE.isDisabled()) {
            var1.render(var2, var3, var4, var5, var6, var7, var8);
        }
    }

    @Inject(method = {"doRender(Lnet/minecraft/entity/item/EntityEnderCrystal;DDDFF)V"}, at = {@At(value = "RETURN")}, cancellable = true)
    public void IdoRender(EntityEnderCrystal var1, double var2, double var4, double var6, float var8, float var9, CallbackInfo var10) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.gameSettings.fancyGraphics = false;
        if (CrystalModify.INSTANCE.isEnabled()) {
            CrystalModify module = CrystalModify.INSTANCE;
            GL11.glPushMatrix();
            float var14 = (float) var1.innerRotation + var9;
            GlStateManager.translate(var2, var4, var6);
            GlStateManager.scale(module.size.getValue(), module.size.getValue(), module.size.getValue());
            Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(ENDER_CRYSTAL_TEXTURES);
            float var15 = MathHelper.sin((var14 * 0.2f)) / 2.0f + 0.5f;
            var15 += var15 * var15;
            float spinSpeed = module.crystalSpeed.getValue();
            float bounceSpeed = module.crystalBounce.getValue();
            if (module.texture.getValue()) {
                if (var1.shouldShowBottom()) {
                    this.modelEnderCrystal.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                } else {
                    this.modelEnderCrystalNoBase.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                }
            }
            GL11.glPushAttrib(1048575);
            if (module.mode.getValue().equals(CrystalModify.modes.Wireframe)) {
                GL11.glPolygonMode(1032, 6913);
            }
            if (module.blendModes.getValue().equals(CrystalModify.BlendModes.Default)) {
                GL11.glBlendFunc(770, 771);
            }
            if (module.blendModes.getValue().equals(CrystalModify.BlendModes.Brighter)) {
                GL11.glBlendFunc(770, 32772);
            }
            GL11.glDisable(3008);
            GL11.glDisable(3553);
            GL11.glDisable(2896);
            GL11.glEnable(3042);
            GL11.glLineWidth(1.5f);
            GL11.glEnable(2960);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glEnable(10754);
            RenderUtil.glColor( module.hidden.getValue() ? module.hiddenColor.getColor() : module.color.getColor());
            if (var1.shouldShowBottom()) {
                this.modelEnderCrystal.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
            } else {
                this.modelEnderCrystalNoBase.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
            }

            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            RenderUtil.glColor(var1 == AutoCrystal.renderCrystal ? Color.RED : module.color.getColor());
            if (var1.shouldShowBottom()) {
                this.modelEnderCrystal.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
            } else {
                this.modelEnderCrystalNoBase.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
            }

            if (module.enchanted.getValue()) {
                mc.getTextureManager().bindTexture(RES_ITEM_GLINT);
                GL11.glTexCoord3d(1.0, 1.0, 1.0);
                GL11.glEnable(3553);
                GL11.glBlendFunc(768, 771);
                RenderUtil.glColor(module.enchantColor.getColor());
                if (var1.shouldShowBottom()) {
                    this.modelEnderCrystal.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                } else {
                    this.modelEnderCrystalNoBase.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                }
                if (module.blendModes.getValue().equals(CrystalModify.BlendModes.Default)) {
                    GL11.glBlendFunc(768, 771);
                }
                if (module.blendModes.getValue().equals(CrystalModify.BlendModes.Brighter)) {
                    GL11.glBlendFunc(770, 32772);
                } else {
                    GL11.glBlendFunc(770, 771);
                }
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            }
            GL11.glEnable(3042);
            GL11.glEnable(2896);
            GL11.glEnable(3553);
            GL11.glEnable(3008);
            GL11.glPopAttrib();


            if (module.outline.getValue()) {
                if (module.outlineMode.getValue().equals(CrystalModify.outlineModes.Wire)) {
                    GL11.glPushAttrib(1048575);
                    GL11.glPolygonMode(1032, 6913);
                    GL11.glDisable(3008);
                    GL11.glDisable(3553);
                    GL11.glDisable(2896);
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glLineWidth(module.lineWidth.getValue());
                    GL11.glEnable(2960);
                    GL11.glDisable(2929);
                    GL11.glDepthMask(false);
                    GL11.glEnable(10754);
                    RenderUtil.glColor(module.outlineColor.getColor());
                    if (var1.shouldShowBottom()) {
                        this.modelEnderCrystal.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                    } else {
                        this.modelEnderCrystalNoBase.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                    }
                    GL11.glEnable(2929);
                    GL11.glDepthMask(true);
                    if (var1.shouldShowBottom()) {
                        this.modelEnderCrystal.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                    } else {
                        this.modelEnderCrystalNoBase.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                    }
                    GL11.glEnable(3042);
                    GL11.glEnable(2896);
                    GL11.glEnable(3553);
                    GL11.glEnable(3008);
                    GL11.glPopAttrib();
                } else {
                    RenderUtil.glColor(module.outlineColor.getColor());
                    RenderUtil.renderOne(module.lineWidth.getValue());
                    if (var1.shouldShowBottom()) {
                        this.modelEnderCrystal.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                    } else {
                        this.modelEnderCrystalNoBase.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                    }
                    RenderUtil.renderTwo();
                    if (var1.shouldShowBottom()) {
                        this.modelEnderCrystal.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                    } else {
                        this.modelEnderCrystalNoBase.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                    }
                    RenderUtil.renderThree();
                    RenderUtil.renderFour(module.outlineColor.getColor());
                    RenderUtil.setColor(module.outlineColor.getColor());
                    if (var1.shouldShowBottom()) {
                        this.modelEnderCrystal.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                    } else {
                        this.modelEnderCrystalNoBase.render(var1, 0.0f, var14 * spinSpeed, var15 * bounceSpeed, 0.0f, 0.0f, 0.0625f);
                    }
                    RenderUtil.renderFive();
                    RenderUtil.setColor(Color.WHITE);
                }
            }
            GL11.glPopMatrix();
        }
    }
}