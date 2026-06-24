package com.realtimes.framework.internal.storage;

import com.realtimes.framework.api.session.SessionContext;
import com.realtimes.framework.api.session.SessionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractSessionStorage implements SessionStorage {

    private static final Logger log = LoggerFactory.getLogger(AbstractSessionStorage.class);

    protected final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();

    @Override
    public boolean addSession(String sessionId, SessionContext context) {
        if (sessionId == null || context == null) {
            log.warn("Invalid addSession request: sessionId={}, context={}", sessionId, context);
            return false;
        }

        sessions.put(sessionId, context);
        return true;
    }

    @Override
    public boolean removeSession(String sessionId) {
        return sessionId != null && sessions.remove(sessionId) != null;
    }

    @Override
    public SessionContext getSession(String sessionId) {
        return sessionId == null ? null : sessions.get(sessionId);
    }

    @Override
    public boolean hasSession(String sessionId) {
        return sessionId != null && sessions.containsKey(sessionId);
    }

    @Override
    public Set<String> getAllSessionIds() {
        return Collections.unmodifiableSet(sessions.keySet());
    }

    @Override
    public int getOnlineSessionCount() {
        return sessions.size();
    }
}

