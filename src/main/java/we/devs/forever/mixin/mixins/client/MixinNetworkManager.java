package we.devs.forever.mixin.mixins.client;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.util.Util;
import we.devs.forever.main.ForeverClient;

@Mixin(NetworkManager.class)
public
class MixinNetworkManager implements Util {

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacketPre(Packet<?> packet, CallbackInfo info) {
        PacketEvent.Send event = new PacketEvent.Send( packet);
        ForeverClient.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void onChannelReadPre(ChannelHandlerContext context, Packet<?> packet, CallbackInfo info) {
        PacketEvent.Receive event = new PacketEvent.Receive( packet);
        ForeverClient.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    /*@Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void exceptionCaughtHook(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_, CallbackInfo info) {
    }*/


//    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
//    private void onSendPacketPre(Packet<?> packet, CallbackInfo info) {
//        PacketEvent.Send event = new PacketEvent.Send(packet);
//        ForeverClient.EVENT_BUS.post(event);
//        if (event.isCanceled()) {
//            info.cancel();
//        } else {
//            if (event.getPacket() instanceof CPacketPlayer.Rotation || event.getPacket() instanceof CPacketPlayer.PositionRotation) {
//            }
//        }
//    }
//
//    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
//    private void onChannelReadPre(ChannelHandlerContext context, Packet<?> packet, CallbackInfo info) {
//        PacketEvent.Receive event = new PacketEvent.Receive(packet);
//        ForeverClient.EVENT_BUS.post(event);
//        if (event.isCanceled()) {
//            info.cancel();
//        }
//    }
//
    @Inject(method = {"sendPacket(Lnet/minecraft/network/Packet;)V"}, at = {@At(value = "RETURN")}, cancellable = true)
    private void onSendPacketPost(Packet<?> packet, CallbackInfo info) {
        PacketEvent.SendPost event = new PacketEvent.SendPost(packet);
        ForeverClient.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = {"channelRead0"}, at = {@At(value = "RETURN")}, cancellable = true)
    private void onChannelReadPost(ChannelHandlerContext context, Packet<?> packet, CallbackInfo info) {
            PacketEvent.ReceivePost event = new PacketEvent.ReceivePost(packet);
            ForeverClient.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                info.cancel();
        }
    }

    /*@Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    private void exceptionCaughtHook(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_, CallbackInfo info) {
    }*/
}
