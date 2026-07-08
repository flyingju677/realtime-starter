package com.realtimes.framework.internal.websocket;

import com.realtimes.framework.properties.RealtimeFrameworkProperties;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WebSocketHeartbeatManager implements SmartLifecycle {

    public static final String FALLBACK_PING_MESSAGE = "__REALTIME_HEARTBEAT_PING__";

    public static final String FALLBACK_PONG_MESSAGE = "__REALTIME_HEARTBEAT_PONG__";

    private static final Logger log = LoggerFactory.getLogger(WebSocketHeartbeatManager.class);

    private static final ByteBuffer PING_PAYLOAD =
            ByteBuffer.wrap("realtime-heartbeat".getBytes(StandardCharsets.UTF_8));

    private static final long MIN_INTERVAL_MILLIS = 1000L;

    private final WebSocketSessionRegistry sessionRegistry;

    private final WebSocketSessionCleaner sessionCleaner;

    private final RealtimeFrameworkProperties.Heartbeat heartbeatProperties;

    private final long effectiveInterval;

    private final long effectiveTimeout;

    private ScheduledExecutorService executorService;

    private ScheduledFuture<?> heartbeatFuture;

    private volatile boolean running;

    public WebSocketHeartbeatManager(WebSocketSessionRegistry sessionRegistry,
                                     WebSocketSessionCleaner sessionCleaner,
                                     RealtimeFrameworkProperties.Heartbeat heartbeatProperties) {
        this.sessionRegistry = sessionRegistry;
        this.sessionCleaner = sessionCleaner;
        this.heartbeatProperties = heartbeatProperties;
        this.effectiveInterval = resolveEffectiveInterval(heartbeatProperties.getInterval());
        this.effectiveTimeout = resolveEffectiveTimeout(heartbeatProperties.getTimeout(), effectiveInterval);
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "realtime-websocket-heartbeat");
            thread.setDaemon(true);
            return thread;
        });
        heartbeatFuture = executorService.scheduleWithFixedDelay(this::heartbeat,
                effectiveInterval, effectiveInterval, TimeUnit.MILLISECONDS);
        running = true;
        log.info("Started realtime websocket heartbeat: interval={}ms, timeout={}ms", effectiveInterval, effectiveTimeout);
    }

    @Override
    public void stop() {
        running = false;
        if (heartbeatFuture != null) {
            heartbeatFuture.cancel(true);
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    void heartbeat() {
        long now = System.currentTimeMillis();
        Set<Session> sessions = sessionRegistry.getAllSessions();
        for (Session session : sessions) {
            handleSessionHeartbeat(session, now);
        }
    }

    private void handleSessionHeartbeat(Session session, long now) {
        if (session == null) {
            return;
        }

        String sessionId = session.getId();
        if (!session.isOpen()) {
            sessionCleaner.cleanup(sessionId);
            return;
        }

        long lastActiveTime = sessionRegistry.getLastActiveTime(sessionId);
        if (lastActiveTime > 0 && now - lastActiveTime > effectiveTimeout) {
            closeTimedOutSession(session, sessionId);
            return;
        }

        sendHeartbeat(session);
    }

    private void sendHeartbeat(Session session) {
        try {
            session.getBasicRemote().sendPing(PING_PAYLOAD.asReadOnlyBuffer());
        } catch (Exception e) {
            sendFallbackHeartbeat(session, e);
        }
    }

    private void sendFallbackHeartbeat(Session session, Exception pingError) {
        try {
            session.getAsyncRemote().sendText(FALLBACK_PING_MESSAGE, result -> {
                if (!result.isOK()) {
                    log.warn("Fallback heartbeat failed: sessionId={}", session.getId(), result.getException());
                }
            });
        } catch (Exception fallbackError) {
            log.warn("Failed to send websocket heartbeat: sessionId={}", session.getId(), pingError);
        }
    }

    private void closeTimedOutSession(Session session, String sessionId) {
        try {
            log.info("Close timeout session... sessionId={}", sessionId);
            if (session.isOpen()) {
                session.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "WebSocket heartbeat timeout"));
            }
        } catch (IOException e) {
            log.warn("Failed to close timed out websocket session: sessionId={}", sessionId, e);
        } finally {
            sessionCleaner.cleanup(sessionId);
        }
    }

    long getEffectiveInterval() {
        return effectiveInterval;
    }

    long getEffectiveTimeout() {
        return effectiveTimeout;
    }

    private long resolveEffectiveInterval(long configuredInterval) {
        if (configuredInterval >= MIN_INTERVAL_MILLIS) {
            return configuredInterval;
        }
        log.warn("realtime.websocket.heartbeat.interval should be at least {}ms, normalized from {}ms to {}ms",
                MIN_INTERVAL_MILLIS, configuredInterval, MIN_INTERVAL_MILLIS);
        return MIN_INTERVAL_MILLIS;
    }

    private long resolveEffectiveTimeout(long configuredTimeout, long interval) {
        long minTimeout = interval * 3;
        if (configuredTimeout >= minTimeout) {
            return configuredTimeout;
        }
        log.warn("realtime.websocket.heartbeat.timeout should be at least 3 times interval, normalized from {}ms to {}ms",
                configuredTimeout, minTimeout);
        return minTimeout;
    }
}
