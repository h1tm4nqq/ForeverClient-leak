package we.devs.forever.client.modules.api.listener;

import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.client.Client;
import we.devs.forever.client.modules.api.Module;

public abstract class ModuleListener<M extends Module, E> extends Listener<E> {

    protected final M module;

    public ModuleListener(M module, Class<E> event) {
        super(event);
        this.module = module;
    }
    public ModuleListener(M module, Class<E> event, ListenerPriority priority) {
        super(event,priority);
        this.module = module;
    }

}
