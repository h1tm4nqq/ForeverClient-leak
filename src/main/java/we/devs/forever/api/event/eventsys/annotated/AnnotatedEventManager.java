package we.devs.forever.api.event.eventsys.annotated;

import we.devs.forever.api.event.eventsys.EventManager;
import we.devs.forever.api.event.eventsys.annotated.dispatch.MethodEventDispatcher;
import we.devs.forever.api.event.eventsys.annotated.filter.MethodFilterScanner;
import we.devs.forever.api.event.eventsys.annotated.handler.MethodEventHandler;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.event.eventsys.dispatch.EventDispatcher;
import we.devs.forever.api.event.eventsys.filter.EventFilterScanner;
import we.devs.forever.api.event.eventsys.handler.EventHandler;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.api.listener.Invoker;
import we.devs.forever.client.modules.api.listener.Listener;
import we.devs.forever.client.modules.api.listener.ModuleListener;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * An {@link EventManager} implementation that uses methods marked
 * with the {@link EventListener}
 * annotation.
 *
 * @author Daniel
 * @since May 31, 2017
 */
public final class AnnotatedEventManager implements EventManager {
    /**
     * Listener scanner implementation used to find all listeners inside
     * of a specific listener.
     */


    /**
     * A map that pairs a listener container with a provided dispatcher implementation.
     */
    private final Map<Object, EventDispatcher> listenerDispatchers = new ConcurrentHashMap<>();
    private final Map<Object, Map<Class<?>, Invoker<?>>> listeners = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <E> E post(final E event) {
        // Iterate the stored dispatchers and notify all listeners

        for (Map<Class<?>, Invoker<?>> methodMap : listeners.values()) {
            if (methodMap.containsKey(event.getClass())) {
                Invoker<E> invoker = (Invoker<E>) methodMap.get(event.getClass());
                //  ForeverClient.LOGGER.info(invoke.getClass().getName() + "  " + event.getClass().getName());
                invoker.invoke(event);

            }
        }
        for (final EventDispatcher dispatcher : listenerDispatchers.values())
            dispatcher.dispatch(event);


        return event;
    }

    @Override
    public boolean isedlistener(final Object listener) {
        return listenerDispatchers.containsKey(listener);
    }

    @Override
    public void register(final Object listenerContainer) {
        // Check if we've already got this object ed
        if (listenerDispatchers.containsKey(listenerContainer))
            return;

        // locate all handlers inside of the container
        final Map<Class<?>, Set<EventHandler>> eventHandlers = locate(listenerContainer);

        if (listenerContainer instanceof Module) {
            Module module = (Module) listenerContainer;
            if (!module.getModuleListeners().isEmpty()) {

                listeners.put(listenerContainer, locateListeners(module));


            }

        }

        if (eventHandlers.isEmpty()) return;



        // create a new dispatcher for this specific listener
        listenerDispatchers.put(listenerContainer, new MethodEventDispatcher(eventHandlers));
    }

    private final EventFilterScanner<Method> filterScanner = new MethodFilterScanner();


    public Map<Class<?>, Set<EventHandler>> locate(final Object listenerContainer) {
        final Map<Class<?>, Set<EventHandler>> eventHandlers = new HashMap<>();
        Stream.of(listenerContainer.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(EventListener.class) && method.getParameterCount() == 1)
                .forEach(method -> eventHandlers
                        .computeIfAbsent(method.getParameterTypes()[0], obj -> new TreeSet<>())
                        .add(new MethodEventHandler(listenerContainer, method, filterScanner.scan(method))));
        return eventHandlers;
    }

    public Map<Class<?>, Invoker<?>> locateListeners(Module module) {
        final Map<Class<?>, Invoker<?>> eventHandlers = new HashMap<>();

        for (Listener<?> listener : module.getModuleListeners()) {

            if(listener.event == null) throw new IllegalStateException("Event cannot be null");
            eventHandlers.put(listener.event, listener);
        }

        return eventHandlers;
    }

    @Override
    public void unregister(final Object listenerContainer) {
        // Remove the given listener container from the dispatchers map
        listenerDispatchers.remove(listenerContainer);
        listeners.remove(listenerContainer);
    }
}
