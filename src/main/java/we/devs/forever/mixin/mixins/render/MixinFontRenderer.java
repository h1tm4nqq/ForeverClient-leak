package we.devs.forever.mixin.mixins.render;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FontRenderer.class) //Could be done easier, we want compatibility with future tho.
public abstract
class MixinFontRenderer {

    @Shadow
    protected abstract int renderString(String text, float x, float y, int color, boolean dropShadow);

    @Redirect(method = "drawString(Ljava/lang/String;FFIZ)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;renderString(Ljava/lang/String;FFIZ)I"))
    public int renderStringHook(FontRenderer fontrenderer, String text, float x, float y, int color, boolean dropShadow) {
        return this.renderString(text, x, y, color, dropShadow);
    }

}
