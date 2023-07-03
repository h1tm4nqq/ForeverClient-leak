package we.devs.forever.mixin.mixins.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.api.event.events.render.RenderItemEvent;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.impl.render.NoRender;
import we.devs.forever.client.modules.impl.render.SmallShield;
import we.devs.forever.client.modules.impl.render.ViewModel;
import we.devs.forever.main.ForeverClient;

@Mixin(ItemRenderer.class)
public abstract
class MixinItemRenderer {

    public Minecraft mc;
    private boolean injection;

    public MixinItemRenderer() {
        this.injection = true;
    }

    @Shadow
    public abstract void renderItemInFirstPerson(final AbstractClientPlayer p0, final float p1, final float p2, final EnumHand p3, final float p4, final ItemStack p5, final float p6);

    @Inject(method = {"renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V"}, at = {@At("HEAD")}, cancellable = true)
    public void renderItemInFirstPersonHook(final AbstractClientPlayer player, final float p_187457_2_, final float p_187457_3_, final EnumHand hand, final float p_187457_5_, final ItemStack stack, final float p_187457_7_, final CallbackInfo info) {
        if (this.injection) {
            info.cancel();
            final SmallShield offset = SmallShield.getInstance();
            float xOffset = 0.0f;
            float yOffset = 0.0f;
            this.injection = false;
            if (hand == EnumHand.MAIN_HAND) {
                if (offset.isEnabled() && player.getHeldItemMainhand() != ItemStack.EMPTY) {
                    xOffset = offset.mainX.getValue();
                    yOffset = offset.mainY.getValue();
                }
            } else if (!(boolean) offset.normalOffset.getValue() && offset.isEnabled() && player.getHeldItemOffhand() != ItemStack.EMPTY) {
                xOffset = offset.offX.getValue();
                yOffset = offset.offY.getValue();
            }
            this.renderItemInFirstPerson(player, p_187457_2_, p_187457_3_, hand, p_187457_5_ + xOffset, stack, p_187457_7_ + yOffset);
            this.injection = true;
        }
    }

    @Inject(method = {"transformSideFirstPerson"}, at = {@At("HEAD")}, cancellable = true)
    public void transformSideFirstPerson(final EnumHandSide hand, final float p_187459_2_, final CallbackInfo ci) {
        final RenderItemEvent event = new RenderItemEvent(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
        ForeverClient.EVENT_BUS.post(event);
        if (ViewModel.getInstance().isEnabled()) {
            ci.cancel();
            final boolean bob =  ViewModel.getInstance().doBob.getValue();
            final int i = (hand == EnumHandSide.RIGHT) ? 1 : -1;
            GlStateManager.translate(i * 0.56f, -0.52f + (bob ? p_187459_2_ : 0.0f) * -0.6f, -0.72f);
            if (hand == EnumHandSide.RIGHT) {
                GlStateManager.translate(event.getMainX(), event.getMainY(), event.getMainZ());
                GlStateManager.scale(event.getMainHandScaleX(), event.getMainHandScaleY(), event.getMainHandScaleZ());
                RenderUtil.rotationHelper((float) event.getMainRotX(), (float) event.getMainRotY(), (float) event.getMainRotZ());
            } else {
                GlStateManager.translate(event.getOffX(), event.getOffY(), event.getOffZ());
                GlStateManager.scale(event.getOffHandScaleX(), event.getOffHandScaleY(), event.getOffHandScaleZ());
                RenderUtil.rotationHelper((float) event.getOffRotX(), (float) event.getOffRotY(), (float) event.getOffRotZ());
            }

        }
    }

    @Redirect(method = {"renderArmFirstPerson"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V", ordinal = 0))
    public void translateHook(final float x, final float y, final float z) {
        final SmallShield offset = SmallShield.getInstance();
        final boolean shiftPos = Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.getHeldItemMainhand() != ItemStack.EMPTY && offset.isEnabled();
        GlStateManager.translate(x + (shiftPos ? offset.mainX.getValue() : 0.0f), y + (shiftPos ? offset.mainY.getValue() : 0.0f), z);
    }

    @Inject(method = {"renderFireInFirstPerson"}, at = {@At("HEAD")}, cancellable = true)
    public void renderFireInFirstPersonHook(final CallbackInfo info) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().fire.getValue()) {
            info.cancel();
        }
    }



    @Inject(method = {"transformEatFirstPerson"}, at = {@At("HEAD")}, cancellable = true)
    private void transformEatFirstPerson(final float p_187454_1_, final EnumHandSide hand, final ItemStack stack, final CallbackInfo ci) {
        if (ViewModel.getInstance().isEnabled()) {
            switch (ViewModel.getInstance().mode.getValue()) {
                case Cancel:
                    ci.cancel();
                    break;
                case Normal:
                    return;
                case Custom:
                    ci.cancel();
                    final float f = Minecraft.getMinecraft().player.getItemInUseCount() - p_187454_1_ + 1.0f;
                    final float f2 = f / stack.getMaxItemUseDuration();
                    if (f2 < 0.8f) {
                        final float f3 = MathHelper.abs(MathHelper.cos(f / 4.0f * 3.1415927f) * ViewModel.getInstance().eat.getValue() * 0.1F);
                        GlStateManager.translate(0.0f, f3, 0.0f);
                    }
                    final float f3 = 1.0f - (float) Math.pow(f2, 27.0);
                    if (hand == EnumHandSide.RIGHT) {
                        GlStateManager.translate(f3 * 0.5f * ViewModel.getInstance().mainXEat.getValue(), f3 * 0.5f * ViewModel.getInstance().mainYEat.getValue(), f3 * 0.5 * -(double) ViewModel.getInstance().mainZEat.getValue());
                        //  GlStateManager.scale(ViewModel.getInstance().mainScaleXEat.getValue(),ViewModel.getInstance().mainScaleYEat.getValue(),ViewModel.getInstance().mainScaleZEat.getValue());
                        GlStateManager.rotate(1 * f3 * 90.0f, 0.0f, 1.0f, 0.0f);
                        GlStateManager.rotate(f3 * 10.0f, 1.0f, 0.0f, 0.0f);
                        GlStateManager.rotate(1 * f3 * 30.0f, 0.0f, 0.0f, 1.0f);

                    } else {
                        GlStateManager.translate(f3 * 0.5f * ViewModel.getInstance().offXEat.getValue(), f3 * 0.5f * ViewModel.getInstance().offYEat.getValue(), f3 * 0.5 * -(double) ViewModel.getInstance().offZEat.getValue());
                        // GlStateManager.scale(ViewModel.getInstance().offScaleXEat.getValue(),ViewModel.getInstance().offScaleYEat.getValue(),ViewModel.getInstance().offScaleZEat.getValue());
                        GlStateManager.rotate(-1 * f3 * 90.0f, 0.0f, 1.0f, 0.0f);
                        GlStateManager.rotate(f3 * 10.0f, 1.0f, 0.0f, 0.0f);
                        GlStateManager.rotate(-1 * f3 * 30.0f, 0.0f, 0.0f, 1.0f);

                    }
            }
        }
    }

    @Inject(method = {"renderSuffocationOverlay"}, at = {@At("HEAD")}, cancellable = true)
    public void renderSuffocationOverlay(final CallbackInfo ci) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().blocks.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "rotateArm", at = @At("HEAD"), cancellable = true)
    public void rotateArmHook(float p_187458_1_, CallbackInfo ci) {
        if (ViewModel.getInstance().isEnabled() && ViewModel.getInstance().noSway.getValue()) {
            ci.cancel();
        }
    }
}

