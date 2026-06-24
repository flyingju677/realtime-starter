package com.realtimes.framework.api.session;

import java.util.Set;

public interface SessionStorage {

    boolean addSession(String sessionId, SessionContext context);

    boolean removeSession(String sessionId);

    SessionContext getSession(String sessionId);

    boolean hasSession(String sessionId);

    Set<String> getAllSessionIds();

    int getOnlineSessionCount();
}

