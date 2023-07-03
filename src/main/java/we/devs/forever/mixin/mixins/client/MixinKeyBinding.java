package we.devs.forever.mixin.mixins.client;


import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import we.devs.forever.api.event.events.client.KeyBindingEvent;
import we.devs.forever.main.ForeverClient;

@Mixin(KeyBinding.class)
public class MixinKeyBinding {

    @Shadow
    public boolean pressed;

    @Inject(method = "isKeyDown", at = @At("RETURN"), cancellable = true)
    private void isKeyDown(CallbackInfoReturnable<Boolean> isKeyDown) {
        KeyBindingEvent event = new KeyBindingEvent(isKeyDown.getReturnValue(), this.pressed);
      ForeverClient.EVENT_BUS.post(event);
        isKeyDown.setReturnValue(event.isHolding());
    }

}
