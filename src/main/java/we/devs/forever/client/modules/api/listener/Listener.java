package we.devs.forever.client.modules.api.listener;

import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.client.Client;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Listener<E> extends Client implements Invoker<E> {
    public Class<E> event;

    public ListenerPriority priority = ListenerPriority.NORMAL;
    public Listener(Class<E> event) {
        this.event =event;
    }
    public Listener(Class<E> event,ListenerPriority priority) {
        this.event =event;
        this.priority = priority;
    }


    public abstract void invoke(E event);

}
