package com.realtimes.framework.api.message;

import com.realtimes.framework.api.subscription.SubscriptionKey;

public interface MessageSender {

    void broadcast(String message);

    void broadcast(SubscriptionKey<?> subscriptionKey, String message);

    void unicast(String sessionId, String message);

    int getOnlineSessionCount();

    int getSubscriberCount(String topic);
}

