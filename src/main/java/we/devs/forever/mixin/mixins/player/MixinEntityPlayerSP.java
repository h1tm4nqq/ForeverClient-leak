package we.devs.forever.mixin.mixins.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.MoverType;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import we.devs.forever.api.event.events.client.ChatEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.events.player.MoveEvent;
import we.devs.forever.api.event.events.player.PlayerUpdateEvent;
import we.devs.forever.api.event.events.player.PushEvent;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.impl.movement.Sprint;
import we.devs.forever.main.ForeverClient;

@Mixin(value = EntityPlayerSP.class)
public class MixinEntityPlayerSP extends AbstractClientPlayer {
    @Shadow
    @Final
    public NetHandlerPlayClient connection;
    @Shadow
    public MovementInput movementInput;
    @Shadow
    public Minecraft mc;

    private MotionEvent motionEvent = new MotionEvent();

    @Shadow
    public boolean isCurrentViewEntity() {
        return false;
    }

    public MixinEntityPlayerSP(Minecraft p_i47378_1_, World p_i47378_2_, NetHandlerPlayClient p_i47378_3_, StatisticsManager p_i47378_4_, RecipeBook p_i47378_5_) {
        super(p_i47378_2_, p_i47378_3_.getGameProfile());
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(String message, CallbackInfo callback) {
        ChatEvent chatEvent = new ChatEvent(message);
        ForeverClient.EVENT_BUS.post(chatEvent);
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
    public void closeScreenHook(EntityPlayerSP entityPlayerSP) {
        /*if(!(BetterPortals.getInstance().isEnabled() && BetterPortals.getInstance().portalChat.getValue())) {
            entityPlayerSP.closeScreen();
        }*/
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void displayGuiScreenHook(Minecraft mc, GuiScreen screen) {
        /*if(!(BetterPortals.getInstance().isEnabled() && BetterPortals.getInstance().portalChat.getValue())) {
            mc.displayGuiScreen(screen);
        }*/
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;setSprinting(Z)V", ordinal = 2))
    public void onLivingUpdate(EntityPlayerSP entityPlayerSP, boolean sprinting) {
        if (Sprint.getInstance().isEnabled() && Sprint.getInstance().mode.getValue() == Sprint.Mode.Rage && (mc.player.movementInput.moveForward != 0.0f || mc.player.movementInput.moveStrafe != 0.0f)) {
            entityPlayerSP.setSprinting(true);
        } else {
            entityPlayerSP.setSprinting(sprinting);
        }
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    public void onPlayerUpdate(CallbackInfo info) {
        ForeverClient.EVENT_BUS.post(new PlayerUpdateEvent());
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void pushOutOfBlocksHook(double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
        PushEvent event = new PushEvent(1,x,y,z);
        ForeverClient.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.setReturnValue(false);
        }
    }
    @Inject(
            method = "onUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V",
                    shift = At.Shift.BEFORE))
    public void onUpdateWalkingPlayerPre(CallbackInfo ci) {
//        ForeverClient.EVENT_BUS.post(new MotionEvent.Pre());
        motionEvent = new MotionEvent.Pre(this.posX, this.getEntityBoundingBox().minY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround);
      //  Client.motionEvent = motionEvent;
        ForeverClient.EVENT_BUS.post(motionEvent);
        posX = motionEvent.getX();
        posY = motionEvent.getY();
        posZ = motionEvent.getZ();
        rotationYaw = motionEvent.getRotationYaw();
        rotationPitch = motionEvent.getRotationPitch();
        onGround = motionEvent.isOnGround();
    }

    @Inject(
            method = "onUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V",
                    shift = At.Shift.AFTER))
    public void onUpdateWalkingPlayerPost(CallbackInfo ci) {
        // maybe someone else changed our position in the meantime
        if (posX == motionEvent.getX()) {
            posX = motionEvent.getInitialX();
        }

        if (posY == motionEvent.getY()) {
            posY = motionEvent.getInitialY();
        }

        if (posZ == motionEvent.getZ()) {
            posZ = motionEvent.getInitialZ();
        }

        if (rotationYaw == motionEvent.getRotationYaw()) {
            rotationYaw = motionEvent.getInitialYaw();
        }

        if (rotationPitch == motionEvent.getRotationPitch()) {
            rotationPitch = motionEvent.getInitialPitch();
        }

        if (onGround == motionEvent.isOnGround()) {
            onGround = motionEvent.isInitialOnGround();
        }
    }

    @Redirect(
            method = "onUpdateWalkingPlayer",
            at = @At(
                    value = "FIELD",
                    target = "net/minecraft/client/entity/EntityPlayerSP.posX:D"))
    public double posXHook(EntityPlayerSP entityPlayerSP) {
        return motionEvent.getX();
    }

    @Redirect(
            method = "onUpdateWalkingPlayer",
            at = @At(
                    value = "FIELD",
                    target = "net/minecraft/util/math/AxisAlignedBB.minY:D"))
    public double minYHook(AxisAlignedBB axisAlignedBB) {
        return motionEvent.getY();
    }

    @Redirect(
            method = "onUpdateWalkingPlayer",
            at = @At(
                    value = "FIELD",
                    target = "net/minecraft/client/entity/EntityPlayerSP.posZ:D"))
    public double posZHook(EntityPlayerSP entityPlayerSP) {
        return motionEvent.getZ();
    }

    @Redirect(
            method = "onUpdateWalkingPlayer",
            at = @At(
                    value = "FIELD",
                    target = "net/minecraft/client/entity/EntityPlayerSP.rotationYaw:F"))
    public float rotationYawHook(EntityPlayerSP entityPlayerSP) {
        return motionEvent.getYaw();
    }

    @Redirect(
            method = "onUpdateWalkingPlayer",
            at = @At(
                    value = "FIELD",
                    target = "net/minecraft/client/entity/EntityPlayerSP.rotationPitch:F"))
    public float rotationPitchHook(EntityPlayerSP entityPlayerSP) {
        return motionEvent.getPitch();
    }

    @Redirect(
            method = "onUpdateWalkingPlayer",
            at = @At(
                    value = "FIELD",
                    target = "net/minecraft/client/entity/EntityPlayerSP.onGround:Z"))
    public boolean onGroundHook(EntityPlayerSP entityPlayerSP) {
        return motionEvent.isOnGround();
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void preMotion(CallbackInfo info) {
        Client.eventManager.updateManagers();
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    private void postMotion(CallbackInfo info) {
        Client.eventManager.resetManagers();
        MotionEvent event = new MotionEvent.Post(motionEvent);
        ForeverClient.EVENT_BUS.post(event);
    }

    @Inject(method = "Lnet/minecraft/client/entity/EntityPlayerSP;setServerBrand(Ljava/lang/String;)V", at = @At("HEAD"))
    public void getBrand(String brand, CallbackInfo callbackInfo) {
        if (Client.serverManager != null) {
            Client.serverManager.setServerBrand(brand);
        }
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void move(AbstractClientPlayer player, MoverType type, double x, double y, double z) {
        final MoveEvent event = new MoveEvent(type, x, y, z);
        ForeverClient.EVENT_BUS.post(event);

        if (!event.isCanceled()) {
            super.move(event.getType(), event.getX(), event.getY(), event.getZ());
        } else {
            super.move(type, x, y, z);
        }
    }
}
