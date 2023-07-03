package we.devs.forever.mixin.mixins.world;

import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.client.Client;

@Mixin(Timer.class)
public class MixinTimer {
    @Shadow
    public float elapsedPartialTicks;

    @Inject(method = "updateTimer", at = @At( value = "FIELD", target = "net/minecraft/util/Timer.elapsedPartialTicks:F", ordinal = 1))
    public void updateTimer(CallbackInfo info) {
        elapsedPartialTicks *= Client.timerManager.getTimer();
        Client.timerManager.priority = 0;
    }
}