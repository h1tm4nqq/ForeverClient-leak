package we.devs.forever.client.modules.impl.combat.autocrystal;

import net.minecraft.network.play.client.CPacketHeldItemChange;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.client.modules.api.listener.ModuleListener;

public class ListenerPacketSend extends ModuleListener<AutoCrystal, PacketEvent.Send> {
    public ListenerPacketSend(AutoCrystal module) {
        super(module, PacketEvent.Send.class);
    }

    @Override
    public void invoke(PacketEvent.Send event) {
        // packet for switching held item
        if (event.getPacket() instanceof CPacketHeldItemChange) {

            // reset our switch time, we just switched
            module.switchTimer.reset();

            // pause switch if item we switched to is not a crystal
            module.autoSwitchTimer.reset();
        }
    }
}
