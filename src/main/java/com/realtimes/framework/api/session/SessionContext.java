package com.realtimes.framework.api.session;

import com.realtimes.framework.api.subscription.SubscriptionKey;

import java.util.Set;

public interface SessionContext {

    String getSessionId();

    <T> T getAttribute(String key, Class<T> type);

    void setAttribute(String key, Object value);

    Set<SubscriptionKey<?>> getSubscriptions();

    void addSubscription(SubscriptionKey<?> subscription);

    void removeSubscription(SubscriptionKey<?> subscription);
}

