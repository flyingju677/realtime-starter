package com.realtimes.framework.internal.websocket;

import com.realtimes.framework.api.message.MessageRouter;
import com.realtimes.framework.api.session.SessionContext;
import com.realtimes.framework.api.subscription.SubscriptionKey;
import com.realtimes.framework.api.subscription.SubscriptionManager;
import com.realtimes.framework.internal.support.SpringContextHolder;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@ServerEndpoint("/websocket/subscribe")
public class DefaultWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(DefaultWebSocketEndpoint.class);

    @OnOpen
    public void onOpen(Session session) {
        WebSocketSessionRegistry sessionRegistry = SpringContextHolder.getBean(WebSocketSessionRegistry.class);
        SessionContext context = new DefaultSessionContext(session);
        sessionRegistry.register(session, context);
        log.info("Realtime websocket session connected: {}, online: {}", session.getId(), sessionRegistry.getOnlineSessionCount());
    }

    @OnClose
    public void onClose(Session session) {
        if (session == null) {
            return;
        }

        cleanupSubscriptions(session.getId());
        WebSocketSessionRegistry sessionRegistry = SpringContextHolder.getBean(WebSocketSessionRegistry.class);
        sessionRegistry.unregister(session);
        log.info("Realtime websocket session closed: {}, online: {}", session.getId(), sessionRegistry.getOnlineSessionCount());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        WebSocketSessionRegistry sessionRegistry = SpringContextHolder.getBean(WebSocketSessionRegistry.class);
        SessionContext context = sessionRegistry.getSessionContext(session.getId());
        if (context == null) {
            log.warn("SessionContext not found, message ignored: sessionId={}", session.getId());
            return;
        }

        SpringContextHolder.getBean(MessageRouter.class).route(message, context);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        String sessionId = session == null ? null : session.getId();
        log.error("Realtime websocket error: sessionId={}", sessionId, error);
    }

    private void cleanupSubscriptions(String sessionId) {
        SubscriptionManager subscriptionManager = SpringContextHolder.getBean(SubscriptionManager.class);
        Set<SubscriptionKey<?>> subscriptions = subscriptionManager.getSubscriptionsBySession(sessionId);
        for (SubscriptionKey<?> subscription : subscriptions) {
            subscriptionManager.unsubscribe(sessionId, subscription);
        }
    }
}

