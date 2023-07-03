package we.devs.forever.mixin.mixins.render;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import we.devs.forever.client.modules.impl.render.Swings;

@Mixin(value={EntityLivingBase.class})
public class MixinEntityLivingBase {

    @Inject(method={"getArmSwingAnimationEnd"}, at={@At(value="HEAD")}, cancellable=true)
    private void getArmSwingAnimationEnd(CallbackInfoReturnable<Integer> info) {
        if (Swings.swings.isEnabled() && Swings.swings.slow.getValue()) {
            info.setReturnValue(Swings.swings.slowSpeed.getValue());

        }
    }
}

