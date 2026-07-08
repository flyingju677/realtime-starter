package com.realtimes.framework.internal.websocket;

import com.realtimes.framework.api.session.SessionContext;
import jakarta.websocket.Session;

import java.util.Set;

public interface WebSocketSessionRegistry {

    void register(Session session, SessionContext context);

    void unregister(Session session);

    void unregister(String sessionId);

    Session getSession(String sessionId);

    SessionContext getSessionContext(String sessionId);

    void refreshActivity(String sessionId);

    long getLastActiveTime(String sessionId);

    Set<Session> getAllSessions();

    int getOnlineSessionCount();
}

