package com.realtimes.framework.api.event;

import com.realtimes.framework.api.subscription.SubscriptionKey;

public interface EventContext {

    SubscriptionKey<?> getSubscriptionKey();

    String getSessionId();

    <T> T getAttribute(String key, Class<T> type);

    void setAttribute(String key, Object value);
}

