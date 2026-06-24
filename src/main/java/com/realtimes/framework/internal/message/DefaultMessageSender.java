package com.realtimes.framework.internal.message;

import com.realtimes.framework.api.message.MessageSender;
import com.realtimes.framework.api.subscription.SubscriptionKey;
import com.realtimes.framework.api.subscription.SubscriptionManager;
import com.realtimes.framework.internal.websocket.WebSocketSessionRegistry;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class DefaultMessageSender implements MessageSender {

    private static final Logger log = LoggerFactory.getLogger(DefaultMessageSender.class);

    private final WebSocketSessionRegistry sessionRegistry;

    private final SubscriptionManager subscriptionManager;

    public DefaultMessageSender(WebSocketSessionRegistry sessionRegistry,
                                        SubscriptionManager subscriptionManager) {
        this.sessionRegistry = sessionRegistry;
        this.subscriptionManager = subscriptionManager;
    }

    @Override
    public void broadcast(String message) {
        for (Session session : sessionRegistry.getAllSessions()) {
            sendMessage(session, message);
        }
    }

    @Override
    public void broadcast(SubscriptionKey<?> subscriptionKey, String message) {
        Set<String> sessionIds = subscriptionManager.getSessionIdsBySubscription(subscriptionKey);
        for (String sessionId : sessionIds) {
            unicast(sessionId, message);
        }
    }

    @Override
    public void unicast(String sessionId, String message) {
        sendMessage(sessionRegistry.getSession(sessionId), message);
    }

    @Override
    public int getOnlineSessionCount() {
        return sessionRegistry.getOnlineSessionCount();
    }

    @Override
    public int getSubscriberCount(String topic) {
        return subscriptionManager.getSubscriberCount(topic);
    }

    private void sendMessage(Session session, String message) {
        if (session == null || !session.isOpen()) {
            return;
        }

        session.getAsyncRemote().sendText(message, result -> {
            if (!result.isOK()) {
                log.error("Failed to send message to session: {}", session.getId(), result.getException());
            }
        });
    }
}

