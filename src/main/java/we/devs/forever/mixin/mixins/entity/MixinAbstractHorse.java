package we.devs.forever.mixin.mixins.entity;

import net.minecraft.entity.passive.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import we.devs.forever.client.modules.impl.movement.EntityControl;

@Mixin(value = {AbstractHorse.class})
public class MixinAbstractHorse {
    @Inject(method = {"isHorseSaddled"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void isHorseSaddled(CallbackInfoReturnable<Boolean> cir) {
        if (EntityControl.INSTANCE.isEnabled()) {
            cir.setReturnValue(true);
        }
    }
}
