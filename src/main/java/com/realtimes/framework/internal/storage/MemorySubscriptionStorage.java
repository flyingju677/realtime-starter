package com.realtimes.framework.internal.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemorySubscriptionStorage extends AbstractSubscriptionStorage {

    private static final Logger log = LoggerFactory.getLogger(MemorySubscriptionStorage.class);

    @Override
    public void cleanupExpiredSubscriptions() {
        log.debug("Memory subscription storage does not maintain independent subscription TTL");
    }
}

