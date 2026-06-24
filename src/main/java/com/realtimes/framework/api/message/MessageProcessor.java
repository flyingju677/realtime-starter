package com.realtimes.framework.api.message;

import com.realtimes.framework.api.session.SessionContext;

public interface MessageProcessor<T> {

    void process(T message, SessionContext context);

    String getSupportedMessageType();
}

