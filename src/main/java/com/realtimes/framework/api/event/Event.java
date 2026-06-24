package com.realtimes.framework.api.event;

public interface Event {

    String getType();

    String getName();

    default int getPriority() {
        return 0;
    }
}

