package we.devs.forever.client.modules.api;

import we.devs.forever.client.Client;
import we.devs.forever.client.modules.api.listener.Listener;
import we.devs.forever.client.modules.api.listener.ModuleListener;

import java.util.ArrayList;
import java.util.List;

public abstract class ModuleHelper<M extends Module> extends Client {
    protected final List<Listener<?>> listeners = new ArrayList<>();

    protected final M module;

    public ModuleHelper(M module) {
        this.module = module;
    }

    public List<Listener<?>> getListeners() {
        return listeners;
    }

}
