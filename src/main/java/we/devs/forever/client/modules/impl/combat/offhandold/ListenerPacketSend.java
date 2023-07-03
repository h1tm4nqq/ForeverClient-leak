package we.devs.forever.client.modules.impl.combat.offhandold;

import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.api.listener.ModuleListener;

public class ListenerPacketSend extends ModuleListener<Offhand, PacketEvent.Send> {
    public ListenerPacketSend(Offhand module) {
        super(module, PacketEvent.Send.class);
    }

    @Override
    public void invoke(PacketEvent.Send event) {
        if (!Client.fullNullCheck() && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL && mc.gameSettings.keyBindUseItem.isKeyDown()) {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                CPacketPlayerTryUseItemOnBlock packet2 = event.getPacket();
                if (packet2.getHand() == EnumHand.MAIN_HAND) {
                    if (module.timer.passedMs(50L)) {
                        mc.player.setActiveHand(EnumHand.OFF_HAND);
                        mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.OFF_HAND));
                    }
                }
                event.cancel();
            } else if (event.getPacket() instanceof CPacketPlayerTryUseItem && ((CPacketPlayerTryUseItem) event.getPacket()).getHand() == EnumHand.OFF_HAND && !module.timer.passedMs(50L)) {
                event.cancel();
            }
        }
    }
}
