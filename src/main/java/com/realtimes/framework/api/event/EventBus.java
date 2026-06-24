package com.realtimes.framework.api.event;

import java.util.List;

public interface EventBus<E extends Event, D> {

    void publish(E event, D data);

    void publish(E event, D data, EventContext context);

    void register(EventListener<E, D> listener);

    void unregister(EventListener<E, D> listener);

    List<EventListener<E, D>> getListeners();

    void start();

    void stop();

    boolean isRunning();
}

