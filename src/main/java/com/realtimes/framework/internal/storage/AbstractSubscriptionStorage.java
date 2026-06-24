package com.realtimes.framework.internal.storage;

import com.realtimes.framework.api.subscription.SubscriptionKey;
import com.realtimes.framework.api.subscription.SubscriptionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSubscriptionStorage implements SubscriptionStorage {

    private static final Logger log = LoggerFactory.getLogger(AbstractSubscriptionStorage.class);

    protected final Map<SubscriptionKey<?>, Set<String>> subscriptionToSessionIds = new ConcurrentHashMap<>();

    protected final Map<String, Set<SubscriptionKey<?>>> sessionToSubscriptions = new ConcurrentHashMap<>();

    @Override
    public boolean addSubscription(String sessionId, SubscriptionKey<?> subscription) {
        if (sessionId == null || subscription == null) {
            log.warn("Invalid addSubscription request: sessionId={}, subscription={}", sessionId, subscription);
            return false;
        }

        subscriptionToSessionIds.computeIfAbsent(subscription, key -> ConcurrentHashMap.newKeySet()).add(sessionId);
        sessionToSubscriptions.computeIfAbsent(sessionId, key -> ConcurrentHashMap.newKeySet()).add(subscription);
        return true;
    }

    @Override
    public boolean removeSubscription(String sessionId, SubscriptionKey<?> subscription) {
        if (sessionId == null || subscription == null) {
            log.warn("Invalid removeSubscription request: sessionId={}, subscription={}", sessionId, subscription);
            return false;
        }

        Set<String> sessionIds = subscriptionToSessionIds.get(subscription);
        if (sessionIds != null) {
            sessionIds.remove(sessionId);
            if (sessionIds.isEmpty()) {
                subscriptionToSessionIds.remove(subscription);
            }
        }

        Set<SubscriptionKey<?>> subscriptions = sessionToSubscriptions.get(sessionId);
        if (subscriptions != null) {
            subscriptions.remove(subscription);
            if (subscriptions.isEmpty()) {
                sessionToSubscriptions.remove(sessionId);
            }
        }
        return true;
    }

    @Override
    public Set<String> getSessionIdsBySubscription(SubscriptionKey<?> subscription) {
        Set<String> sessionIds = subscriptionToSessionIds.get(subscription);
        return sessionIds == null ? Collections.emptySet() : new HashSet<>(sessionIds);
    }

    @Override
    public Set<SubscriptionKey<?>> getSubscriptionsBySession(String sessionId) {
        Set<SubscriptionKey<?>> subscriptions = sessionToSubscriptions.get(sessionId);
        return subscriptions == null ? Collections.emptySet() : new HashSet<>(subscriptions);
    }

    @Override
    public boolean hasSubscribers(SubscriptionKey<?> subscription) {
        Set<String> sessionIds = subscriptionToSessionIds.get(subscription);
        return sessionIds != null && !sessionIds.isEmpty();
    }

    @Override
    public int getSubscriberCount(String topic) {
        if (topic == null) {
            return 0;
        }

        Set<String> subscribers = new HashSet<>();
        for (Map.Entry<SubscriptionKey<?>, Set<String>> entry : subscriptionToSessionIds.entrySet()) {
            SubscriptionKey<?> subscription = entry.getKey();
            if (subscription != null && Objects.equals(topic, subscription.getTopic())) {
                subscribers.addAll(entry.getValue());
            }
        }
        return subscribers.size();
    }

    @Override
    public int getTotalSubscriptionCount() {
        return subscriptionToSessionIds.size();
    }
}

