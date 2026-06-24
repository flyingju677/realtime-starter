package com.realtimes.framework.internal.websocket;

import com.realtimes.framework.api.session.SessionContext;
import com.realtimes.framework.api.subscription.SubscriptionKey;
import jakarta.websocket.Session;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSessionContext implements SessionContext {

    private final Session session;

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    private final Set<SubscriptionKey<?>> subscriptions = ConcurrentHashMap.newKeySet();

    public DefaultSessionContext(Session session) {
        this.session = session;
    }

    public Session getNativeSession() {
        return session;
    }

    @Override
    public String getSessionId() {
        return session.getId();
    }

    @Override
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value != null && type != null) {
            return type.cast(value);
        }
        return null;
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public Set<SubscriptionKey<?>> getSubscriptions() {
        return Collections.unmodifiableSet(subscriptions);
    }

    @Override
    public void addSubscription(SubscriptionKey<?> subscription) {
        subscriptions.add(subscription);
    }

    @Override
    public void removeSubscription(SubscriptionKey<?> subscription) {
        subscriptions.remove(subscription);
    }
}

