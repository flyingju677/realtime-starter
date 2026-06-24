package com.realtimes.framework.internal.message;

import com.realtimes.framework.api.message.MessageProcessor;
import com.realtimes.framework.api.message.MessageRouter;
import com.realtimes.framework.api.session.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractMessageRouter implements MessageRouter {

    private static final Logger log = LoggerFactory.getLogger(AbstractMessageRouter.class);

    protected final Map<String, MessageProcessor<?>> processorMap = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void route(String message, SessionContext context) {
        if (message == null || message.isBlank()) {
            log.warn("Invalid route request: message is blank");
            return;
        }

        String messageType = extractMessageType(message);
        if (messageType == null || messageType.isBlank()) {
            log.warn("Cannot extract message type from message: {}", message);
            return;
        }

        MessageProcessor<String> processor = (MessageProcessor<String>) processorMap.get(messageType);
        if (processor == null) {
            log.warn("No processor found for message type: {}", messageType);
            return;
        }

        try {
            processor.process(message, context);
        } catch (Exception e) {
            log.error("Error processing message with type: {}", messageType, e);
        }
    }

    @Override
    public void registerProcessor(MessageProcessor<?> processor) {
        if (processor == null) {
            log.warn("Attempt to register null processor");
            return;
        }

        String messageType = processor.getSupportedMessageType();
        if (messageType == null || messageType.isBlank()) {
            log.warn("Processor returned blank message type: {}", processor.getClass().getSimpleName());
            return;
        }

        processorMap.put(messageType, processor);
        log.debug("Registered processor: {} for message type: {}", processor.getClass().getSimpleName(), messageType);
    }

    @Override
    public void unregisterProcessor(MessageProcessor<?> processor) {
        if (processor == null || processor.getSupportedMessageType() == null) {
            return;
        }
        processorMap.remove(processor.getSupportedMessageType());
    }

    protected abstract String extractMessageType(String message);
}

