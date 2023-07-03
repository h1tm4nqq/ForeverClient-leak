package we.devs.forever.mixin.mixins.render;


import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.impl.client.hud.HUD;
import we.devs.forever.client.modules.impl.render.NoRender;

@Mixin(GuiIngame.class)
public
class MixinGuiIngame extends Gui {

    @Inject(method = "renderPortal", at = @At("HEAD"), cancellable = true)
    protected void renderPortalHook(float n, ScaledResolution scaledResolution, CallbackInfo info) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().portal.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderPumpkinOverlay", at = @At("HEAD"), cancellable = true)
    protected void renderPumpkinOverlayHook(ScaledResolution scaledRes, CallbackInfo info) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().pumpkin.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderPotionEffects", at = @At("HEAD"), cancellable = true)
    protected void renderPotionEffectsHook(ScaledResolution scaledRes, CallbackInfo info) {
        if (Client.moduleManager != null && HUD.getInstance().potionIcons.getValue()) {
            info.cancel();
        }
    }
}
