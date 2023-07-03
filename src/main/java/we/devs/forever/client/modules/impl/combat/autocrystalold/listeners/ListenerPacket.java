package we.devs.forever.client.modules.impl.combat.autocrystalold.listeners;

import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketUseEntity;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.client.modules.api.listener.ModuleListener;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;


public class ListenerPacket extends ModuleListener<AutoCrystal, PacketEvent.Send> {

    public ListenerPacket(AutoCrystal module) {
        super(module, PacketEvent.Send.class);
    }

    @Override
    public void invoke(PacketEvent.Send event) {
        if(event.getPacket() instanceof CPacketHeldItemChange){
          module.switchTimer.reset();
        }

//        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock
//                && mc.player.getHeldItem(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getHand()).getItem() == Items.END_CRYSTAL) {
//           // module.placedList.add(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos());
//            module.placeLocations.put(((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos(), System.currentTimeMillis());
//        }
//
        if (!module.cancelcrystal.getValue() || event.getStage() != 0) return;
        if (!(event.getPacket() instanceof CPacketUseEntity)) return;

        CPacketUseEntity packet = event.getPacket();
        if (packet.getAction() == CPacketUseEntity.Action.ATTACK) {
            if (module.lastHitEntity != null) {
                module.lastHitEntity.setDead();
                mc.world.removeEntityFromWorld(module.lastHitEntity.getEntityId());
            }

        }


    }
}
