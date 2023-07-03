package we.devs.forever.api.util;

import net.minecraftforge.common.MinecraftForge;
import we.devs.forever.api.event.eventsys.dispatch.EventDispatcher;
import we.devs.forever.client.Client;
import we.devs.forever.main.ForeverClient;

public class ListenedUtil extends Client implements Util, EventDispatcher {
    public void load() {
        ForeverClient.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public <E> void dispatch(E event) {

    }
}
