package com.realtimes.framework.api.subscription;

public interface SubscriptionKey<T> {

    String getTopic();

    T getBusinessKey();

    default String getUniqueKey() {
        T businessKey = getBusinessKey();
        return getTopic() + ":" + (businessKey == null ? "" : businessKey);
    }
}

