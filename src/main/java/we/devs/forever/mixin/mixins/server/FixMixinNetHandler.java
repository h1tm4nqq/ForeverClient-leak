package we.devs.forever.mixin.mixins.server;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import we.devs.forever.api.util.Util;

/**
 * We need fix switching slot
 * because vanilla method getBestHotbarSlot breaks our switches (when hotbar has empty slots)
 * UPD:
 * Another idea: we wil take EntityPlayerMP and send our owns swaps
 */
@Mixin(NetHandlerPlayServer.class) //NetHandlerPlayServer
public abstract class FixMixinNetHandler implements Util {

    @Shadow
    public EntityPlayerMP player;

//    @Inject(method = "processCustomPayload", at = @At("HEAD"))
//    public void getPlayer(CPacketCustomPayload packetIn, CallbackInfo ci) {
//        Client.fixManager.setObjectToSwap(player);
//    }

//    @Redirect(method = "processCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;pickItem(I)V"))
//    public void fixItemStack(InventoryPlayer instance, int index) {
//        ItemStack itemStack = player.inventory.mainInventory.get(player.inventory.currentItem);
//        player.inventory.mainInventory.set(player.inventory.currentItem, player.inventory.mainInventory.get(index));
//        player.inventory.mainInventory.set(index, itemStack);
//
//    }
//
//    @Redirect(method = "processCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V"))
//    public void fixPacket(NetHandlerPlayServer instance, Packet<?> entityplayer$enumchatvisibility) {
//        //We need to send only one packet
////        player.connection.sendPacket(new SPacketHeldItemChange(this.player.inventory.currentItem));
//    }

//    @Inject(method = "processCustomPayload", at = @At("RETURN"))
//    public void syncItem(CPacketCustomPayload packetIn, CallbackInfo ci) {
//
//    }


}
