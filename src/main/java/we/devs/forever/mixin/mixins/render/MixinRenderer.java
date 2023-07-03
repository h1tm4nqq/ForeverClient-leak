package we.devs.forever.mixin.mixins.render;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({Render.class})
abstract class MixinRenderer<T extends Entity> {
    @Shadow
    protected boolean renderOutlines;
    @Shadow
    @Final
    protected RenderManager renderManager;

    @Shadow
    protected abstract boolean bindEntityTexture(final T p0);

    @Shadow
    protected abstract int getTeamColor(final T p0);

}
