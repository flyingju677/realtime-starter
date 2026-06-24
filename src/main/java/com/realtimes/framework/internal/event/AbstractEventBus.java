package com.realtimes.framework.internal.event;

import com.realtimes.framework.api.event.Event;
import com.realtimes.framework.api.event.EventBus;
import com.realtimes.framework.api.event.EventContext;
import com.realtimes.framework.api.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public abstract class AbstractEventBus<E extends Event, D> implements EventBus<E, D> {

    private static final Logger log = LoggerFactory.getLogger(AbstractEventBus.class);

    protected final Map<Class<? extends E>, List<EventListener<E, D>>> listenerMap = new ConcurrentHashMap<>();

    protected volatile boolean running = false;

    @Override
    public void publish(E event, D data) {
        publish(event, data, null);
    }

    @Override
    public void publish(E event, D data, EventContext context) {
        if (!running) {
            log.warn("EventBus is not running, event ignored: {}", event);
            return;
        }

        List<EventListener<E, D>> listeners = findListeners(event);
        if (listeners.isEmpty()) {
            log.debug("No listeners for event: {}", event);
            return;
        }

        dispatchEvent(event, data, context, listeners);
    }

    protected abstract void dispatchEvent(E event, D data, EventContext context,
                                          List<EventListener<E, D>> listeners);

    @Override
    public void register(EventListener<E, D> listener) {
        if (listener == null) {
            log.warn("Attempt to register null listener");
            return;
        }

        Set<Class<? extends E>> eventTypes = listener.getAcceptedEventTypes();
        for (Class<? extends E> eventType : eventTypes) {
            listenerMap.compute(eventType, (key, listeners) -> {
                if (listeners == null) {
                    listeners = new CopyOnWriteArrayList<>();
                }
                listeners.add(listener);
                return listeners;
            });
        }
        log.debug("Registered event listener: {}", listener.getName());
    }

    @Override
    public void unregister(EventListener<E, D> listener) {
        if (listener == null) {
            log.warn("Attempt to unregister null listener");
            return;
        }

        for (Class<? extends E> eventType : listener.getAcceptedEventTypes()) {
            listenerMap.computeIfPresent(eventType, (key, listeners) -> {
                listeners.remove(listener);
                return listeners.isEmpty() ? null : listeners;
            });
        }
    }

    @Override
    public List<EventListener<E, D>> getListeners() {
        return listenerMap.values().stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void start() {
        this.running = true;
        log.info("EventBus started");
    }

    @Override
    public void stop() {
        this.running = false;
        log.info("EventBus stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    protected List<EventListener<E, D>> findListeners(E event) {
        List<EventListener<E, D>> result = new ArrayList<>();
        for (Map.Entry<Class<? extends E>, List<EventListener<E, D>>> entry : listenerMap.entrySet()) {
            if (entry.getKey().isInstance(event)) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }
}

