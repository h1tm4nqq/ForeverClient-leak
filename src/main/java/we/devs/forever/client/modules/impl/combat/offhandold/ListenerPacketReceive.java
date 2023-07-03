package we.devs.forever.client.modules.impl.combat.offhandold;

import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.client.modules.api.listener.ModuleListener;

public class ListenerPacketReceive extends ModuleListener<Offhand, PacketEvent.Receive> {
    public ListenerPacketReceive(Offhand module) {
        super(module, PacketEvent.Receive.class);
    }

    @Override
    public void invoke(PacketEvent.Receive event) {

    }
}
