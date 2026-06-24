package com.realtimes.framework.api.event;

import java.util.Set;

public interface EventListener<E extends Event, D> {

    Set<Class<? extends E>> getAcceptedEventTypes();

    void onEvent(E event, D data, EventContext context);

    default String getName() {
        return getClass().getSimpleName();
    }

    default boolean isAsync() {
        return true;
    }
}

