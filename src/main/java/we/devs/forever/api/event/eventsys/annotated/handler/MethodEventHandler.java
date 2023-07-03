package we.devs.forever.api.event.eventsys.annotated.handler;

import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.event.eventsys.filter.EventFilter;
import we.devs.forever.api.event.eventsys.handler.EventHandler;
import we.devs.forever.api.event.eventsys.handler.ListenerPriority;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * A basic implementation of the {@link EventHandler} used to
 * mark a method as an event listener via handle.
 *
 * @author Daniel
 * @since May 31, 2017
 */
public final class MethodEventHandler implements EventHandler {
    /**
     * The class instance and parent of the method.
     */
    public final Object listenerParent;

    /**
     * The method object that has been marked as a listener.
     */
    public final Method method;

    /**
     * A filter predicate used to test the passed method against
     * all ed filters on the handler.
     */
    public Set<EventFilter> eventFilters;
    public boolean isModule = false;

    /**
     * The annotation to the event listener.
     */
    public ListenerPriority priority;
    Class<?> event;

    public EventListener eventListenerAnnotation;

    public MethodEventHandler(Object listenerParent, Method method, Set<EventFilter> eventFilters) {
        this.listenerParent = listenerParent;
        if (!method.isAccessible()) method.setAccessible(true);

        this.method = method;
        this.eventFilters = eventFilters;
        this.eventListenerAnnotation = method.getAnnotation(EventListener.class);
    }

    public MethodEventHandler(Object listenerParent, Method method, ListenerPriority priority, Class<?> event) {
        this.listenerParent = listenerParent;
        this.method = method;
        this.priority = priority;
        this.event = event;
        isModule = true;

    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> void handle(final E event) {
        // iterate all event filters given to the handler
        if (eventListenerAnnotation != null) {
            for (final EventFilter filter : eventFilters)
                if (!filter.test(this, event))
                    return;
        }


        try {
//            if(isModule) {
//                ForeverClient.LOGGER.info("Event: " + event.toString() + "\nMethod: " + method.getName() + "\n Class: " + listenerParent.getClass().getName());
//            }
            // invoke the listener with the current event
            // ForeverClient.LOGGER.info(event.toString());

            method.invoke(listenerParent, event);


        } catch (final IllegalAccessException | InvocationTargetException | IllegalArgumentException exception) {
            exception.printStackTrace();
            //ForeverClient.LOGGER.error("Caught exception, Event: " + event.getClass().getName() + " " + this.event.getName());
        }
    }

    @Override
    public Object getListener() {
        return method;
    }

    @Override
    public ListenerPriority getPriority() {
        if (eventListenerAnnotation == null)
            return priority;

        else
            return eventListenerAnnotation.priority();
    }

    @Override
    public Iterable<EventFilter> getFilters() {
        return eventFilters;
    }

    @Override
    public int compareTo(final EventHandler eventHandler) {
        return Integer.compare(eventHandler.getPriority().getPriorityLevel(), getPriority().getPriorityLevel());
    }
}
