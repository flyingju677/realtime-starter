package com.realtimes.framework.api.subscription;

import java.util.Set;

public interface SubscriptionStorage {

    boolean addSubscription(String sessionId, SubscriptionKey<?> subscription);

    boolean removeSubscription(String sessionId, SubscriptionKey<?> subscription);

    Set<String> getSessionIdsBySubscription(SubscriptionKey<?> subscription);

    Set<SubscriptionKey<?>> getSubscriptionsBySession(String sessionId);

    boolean hasSubscribers(SubscriptionKey<?> subscription);

    int getSubscriberCount(String topic);

    void cleanupExpiredSubscriptions();

    int getTotalSubscriptionCount();
}

