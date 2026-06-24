package com.realtimes.framework.internal.event;

import com.realtimes.framework.api.event.Event;
import com.realtimes.framework.api.event.EventContext;
import com.realtimes.framework.api.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

public class MemoryEventBus<E extends Event, D> extends AbstractEventBus<E, D> {

    private static final Logger log = LoggerFactory.getLogger(MemoryEventBus.class);

    private final ThreadPoolTaskExecutor taskExecutor;

    public MemoryEventBus(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    protected void dispatchEvent(E event, D data, EventContext context,
                                 List<EventListener<E, D>> listeners) {
        for (EventListener<E, D> listener : listeners) {
            if (listener.isAsync() && taskExecutor != null) {
                try {
                    taskExecutor.execute(() -> invokeListener(listener, event, data, context));
                } catch (Exception e) {
                    log.error("Failed to submit async event listener: {}", listener.getName(), e);
                }
            } else {
                invokeListener(listener, event, data, context);
            }
        }
    }

    private void invokeListener(EventListener<E, D> listener, E event, D data,
                                EventContext context) {
        try {
            listener.onEvent(event, data, context);
        } catch (Exception e) {
            log.error("Error processing event in listener: {}", listener.getName(), e);
        }
    }
}

