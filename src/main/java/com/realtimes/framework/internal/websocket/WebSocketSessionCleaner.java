package com.realtimes.framework.internal.websocket;

import com.realtimes.framework.api.subscription.SubscriptionKey;
import com.realtimes.framework.api.subscription.SubscriptionManager;

import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketSessionCleaner {

    private final WebSocketSessionRegistry sessionRegistry;

    private final SubscriptionManager subscriptionManager;

    public WebSocketSessionCleaner(WebSocketSessionRegistry sessionRegistry,
                                   SubscriptionManager subscriptionManager) {
        this.sessionRegistry = sessionRegistry;
        this.subscriptionManager = subscriptionManager;
    }

    public void cleanup(String sessionId) {
        if (sessionId == null) {
            return;
        }

        if (sessionRegistry.getSession(sessionId) == null) {
            return;
        }

        log.info("Clean up session: sessionId={}", sessionId);

        Set<SubscriptionKey<?>> subscriptions = subscriptionManager.getSubscriptionsBySession(sessionId);
        for (SubscriptionKey<?> subscription : subscriptions) {
            subscriptionManager.unsubscribe(sessionId, subscription);
        }
        sessionRegistry.unregister(sessionId);
    }
}
