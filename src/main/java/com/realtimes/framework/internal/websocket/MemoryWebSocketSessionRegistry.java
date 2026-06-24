package com.realtimes.framework.internal.websocket;

import com.realtimes.framework.api.session.SessionContext;
import com.realtimes.framework.api.session.SessionStorage;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryWebSocketSessionRegistry implements WebSocketSessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(MemoryWebSocketSessionRegistry.class);

    private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    private final Set<Session> sessionPool = ConcurrentHashMap.newKeySet();

    private final SessionStorage sessionStorage;

    public MemoryWebSocketSessionRegistry(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void register(Session session, SessionContext context) {
        if (session == null) {
            log.warn("Invalid session register request: session is null");
            return;
        }

        sessionPool.add(session);
        sessionMap.put(session.getId(), session);
        if (context != null) {
            sessionStorage.addSession(session.getId(), context);
        }
    }

    @Override
    public void unregister(Session session) {
        if (session != null) {
            unregister(session.getId());
            sessionPool.remove(session);
        }
    }

    @Override
    public void unregister(String sessionId) {
        if (sessionId == null) {
            return;
        }
        Session session = sessionMap.remove(sessionId);
        if (session != null) {
            sessionPool.remove(session);
        }
        sessionStorage.removeSession(sessionId);
    }

    @Override
    public Session getSession(String sessionId) {
        return sessionId == null ? null : sessionMap.get(sessionId);
    }

    @Override
    public SessionContext getSessionContext(String sessionId) {
        return sessionStorage.getSession(sessionId);
    }

    @Override
    public Set<Session> getAllSessions() {
        return new HashSet<>(sessionPool);
    }

    @Override
    public int getOnlineSessionCount() {
        return sessionPool.size();
    }
}

