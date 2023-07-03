package we.devs.forever.mixin.mixins.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import we.devs.forever.api.event.events.player.TravelEvent;
import we.devs.forever.main.ForeverClient;

@Mixin(EntityPlayer.class)
public abstract
class MixinEntityPlayer extends EntityLivingBase {

    public MixinEntityPlayer(World worldIn, GameProfile gameProfileIn) {
        super(worldIn);
    }

    @Inject(method = "getCooldownPeriod", at = @At("HEAD"))
    private void getCooldownPeriodHook(CallbackInfoReturnable<Float> callbackInfoReturnable) {
        /*if (TpsSync.getInstance().isEnabled() && TpsSync.getInstance().attack.getValue()) {
            callbackInfoReturnable.setReturnValue((float)(1.0 / EntityPlayer.class.cast(this).getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue() * 20.0 * nekoplus.serverManager.getTpsFactor()));
        }*/
    }

//    @Inject(method = "updateEntityActionState", at = @At("RETURN"))
//    public void onUpdateEntityAction(CallbackInfo ci) {
//        Client.rotationManager.rotationPitchHead = this.rotationPitch;
//    }

    @ModifyConstant(method = "getPortalCooldown", constant = @Constant(intValue = 10))
    private int getPortalCooldownHook(int cooldown) {
        int time = cooldown;
        /*if(BetterPortals.getInstance().isEnabled() && BetterPortals.getInstance().fastPortal.getValue()) {
            time = BetterPortals.getInstance().cooldown.getValue();
        }*/
        return time;
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(float strafe, float vertical, float forward, CallbackInfo info) {
        EntityPlayer us = (EntityPlayer) (Object) this;
        if (!(us instanceof EntityPlayerSP))
            return;
        TravelEvent event = new TravelEvent(strafe, vertical, forward);
        ForeverClient.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            move(MoverType.SELF, motionX, motionY, motionZ);
            info.cancel();
        }
    }

    @Inject(method = "isEntityInsideOpaqueBlock", at = @At("HEAD"), cancellable = true)
    private void isEntityInsideOpaqueBlockHook(CallbackInfoReturnable<Boolean> info) {
    }

}
