package com.realtimes.framework.api.subscription;

import java.util.Set;

public interface SubscriptionManager {

    void subscribe(String sessionId, SubscriptionKey<?> subscription);

    void unsubscribe(String sessionId, SubscriptionKey<?> subscription);

    Set<String> getSessionIdsBySubscription(SubscriptionKey<?> subscription);

    Set<SubscriptionKey<?>> getSubscriptionsBySession(String sessionId);

    boolean hasSubscribers(SubscriptionKey<?> subscription);

    int getSubscriberCount(String topic);

    void cleanupExpiredSubscriptions();
}

