package we.devs.forever.api.manager.api;

import net.minecraftforge.common.MinecraftForge;
import we.devs.forever.client.Client;
import we.devs.forever.main.ForeverClient;

public abstract class AbstractManager extends Client {
    public AbstractManager(String name) {
        super(name);
    }
    public AbstractManager(String name, boolean load) {
        super(name);
        if (load) load();
    }

    protected abstract void onLoad();
    protected abstract void onUnload();

    public final void unload() {
        ForeverClient.EVENT_BUS.unregister(this);
        MinecraftForge.EVENT_BUS.unregister(this);
        onUnload();
    }
    public final void load() {
        ForeverClient.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(this);
        onLoad();
    }
}
