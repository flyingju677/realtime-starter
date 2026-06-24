package com.realtimes.framework.api.message;

import com.realtimes.framework.api.session.SessionContext;

public interface MessageRouter {

    void route(String message, SessionContext context);

    void registerProcessor(MessageProcessor<?> processor);

    void unregisterProcessor(MessageProcessor<?> processor);
}

