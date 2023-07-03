package we.devs.forever.api.event.eventsys;

import we.devs.forever.api.event.eventsys.handler.EventHandler;

/**
 * Pomelo is a simplistic event-bus that supports event filtering.
 * <p>
 * todo; thread-safety
 * todo; junit testing
 *
 * @author Daniel
 * @since May 31, 2017
 */
public interface EventManager {
    /**
     * Notify all ed {@link EventHandler}s that are listening
     * for the passed event that the event has been dispatched.
     *
     * @param event event instance
     * @param <E>   event type
     * @return passed event instance
     */
    <E> E post(E event);
    /**
     * Checks if the given listener object is ed.
     *
     * @param listener listener instance
     * @return true if ed; false otherwise
     */
    boolean isedlistener(Object listener);
    /**
     * an object as an event listener that listens for the provided
     * eventClass type to be dispatched.
     *
     * @param listener event listener instance
     */
    void register(Object listener);
    /**
     * Remove an event listener from the bus so it does not listen
     * for event dispatches anymore.
     *
     * @param listener event listener instance
     */
    void unregister(Object listener);
}
