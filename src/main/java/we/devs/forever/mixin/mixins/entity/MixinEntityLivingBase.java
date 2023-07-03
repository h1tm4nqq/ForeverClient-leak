package we.devs.forever.mixin.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.api.event.events.player.ElytraEvent;
import we.devs.forever.main.ForeverClient;

@Mixin(EntityLivingBase.class)
public abstract
class MixinEntityLivingBase extends Entity {


    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(World worldIn, CallbackInfo ci) {
//        Client.rotationManager.setRotationPitchHead(this.rotationPitch);
//        Command.sendMessage("1 rotPitchClient: " + rotationPitch);
//        Command.sendMessage("2 rotPitchPacket: " + Client.rotationManager.getRotationPitchHead());
    }

    @Inject(method = "onEntityUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;prevRotationYawHead:F", shift = At.Shift.AFTER))
    public void onDo(CallbackInfo ci) {
//        Client.rotationManager.setPrevRotationPitchHead(Client.rotationManager.getRotationPitchHead());
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void onTravel(float strafe, float vertical, float forward, CallbackInfo ci) {
        ElytraEvent event = new ElytraEvent((EntityLivingBase) (Object) this);
        ForeverClient.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}