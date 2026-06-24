package com.realtimes.framework.internal.subscription;

import com.realtimes.framework.api.session.SessionContext;
import com.realtimes.framework.api.session.SessionStorage;
import com.realtimes.framework.api.subscription.SubscriptionKey;
import com.realtimes.framework.api.subscription.SubscriptionManager;
import com.realtimes.framework.api.subscription.SubscriptionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class DefaultSubscriptionManager implements SubscriptionManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultSubscriptionManager.class);

    private final SubscriptionStorage subscriptionStorage;

    private final SessionStorage sessionStorage;

    public DefaultSubscriptionManager(SubscriptionStorage subscriptionStorage,
                                              SessionStorage sessionStorage) {
        this.subscriptionStorage = subscriptionStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void subscribe(String sessionId, SubscriptionKey<?> subscription) {
        if (sessionId == null || subscription == null) {
            log.warn("Invalid subscribe request: sessionId={}, subscription={}", sessionId, subscription);
            return;
        }

        subscriptionStorage.addSubscription(sessionId, subscription);
        SessionContext context = sessionStorage.getSession(sessionId);
        if (context != null) {
            context.addSubscription(subscription);
        }
    }

    @Override
    public void unsubscribe(String sessionId, SubscriptionKey<?> subscription) {
        if (sessionId == null || subscription == null) {
            log.warn("Invalid unsubscribe request: sessionId={}, subscription={}", sessionId, subscription);
            return;
        }

        subscriptionStorage.removeSubscription(sessionId, subscription);
        SessionContext context = sessionStorage.getSession(sessionId);
        if (context != null) {
            context.removeSubscription(subscription);
        }
    }

    @Override
    public Set<String> getSessionIdsBySubscription(SubscriptionKey<?> subscription) {
        return subscriptionStorage.getSessionIdsBySubscription(subscription);
    }

    @Override
    public Set<SubscriptionKey<?>> getSubscriptionsBySession(String sessionId) {
        return subscriptionStorage.getSubscriptionsBySession(sessionId);
    }

    @Override
    public boolean hasSubscribers(SubscriptionKey<?> subscription) {
        return subscriptionStorage.hasSubscribers(subscription);
    }

    @Override
    public int getSubscriberCount(String topic) {
        return subscriptionStorage.getSubscriberCount(topic);
    }

    @Override
    public void cleanupExpiredSubscriptions() {
        subscriptionStorage.cleanupExpiredSubscriptions();
    }
}

