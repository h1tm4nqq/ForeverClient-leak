package we.devs.forever.api.event.eventsys.annotated.filter;

import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.event.eventsys.filter.EventFilter;
import we.devs.forever.api.event.eventsys.filter.EventFilterScanner;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of the filter scanner that locates specific
 * filters based on annotations and reflection.
 *
 * @author Daniel
 * @since Jun 14, 2017
 */
public final class MethodFilterScanner implements EventFilterScanner<Method> {

    @Override
    public Set<EventFilter> scan(final Method listener) {
        if (!listener.isAnnotationPresent(EventListener.class))
            return Collections.emptySet();

        final Set<EventFilter> filters = new HashSet<>();
        // iterate all filters in the annotation and instantiate them
        for (final Class<? extends EventFilter> filter : listener.getDeclaredAnnotation(EventListener.class).filters())
            try {
                filters.add(filter.newInstance());
            } catch (final Exception exception) {
                exception.printStackTrace();
            }

        return filters;
    }
}
