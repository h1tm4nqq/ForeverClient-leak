package we.devs.forever.client.modules.impl.render.logoutspots;

import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.client.modules.api.listener.ModuleListener;

public class ListenerPacketRecieve extends ModuleListener<LogoutSpots,PacketEvent.Receive> {

    public ListenerPacketRecieve(LogoutSpots module) {
        super(module, PacketEvent.Receive.class);
    }

    @Override
    public void invoke(PacketEvent.Receive event) {

    }
}
