package com.realtimes.framework.internal.websocket;

import com.realtimes.framework.api.message.MessageRouter;
import com.realtimes.framework.api.session.SessionContext;
import com.realtimes.framework.internal.support.SpringContextHolder;
import com.realtimes.framework.properties.RealtimeFrameworkProperties;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.PongMessage;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket端点类，负责WS连接的生命周期
 *
 * Jakarta WebSocket 有两种主要写法：
 * 1. 注解式：@ServerEndpoint + @OnOpen / @OnMessage
 * 2. 编程式：继承 Endpoint + ServerEndpointConfig
 * 要动态配置 URL，就不能继续依赖 @ServerEndpoint("/固定路径")，所以改成编程式注册。Endpoint是 JSR-356提供的编程式服务端端点基类，配合 ServerEndpointConfig 可以在运行时指定 path。
 */
public class DefaultWebSocketEndpoint extends Endpoint {

    private static final Logger log = LoggerFactory.getLogger(DefaultWebSocketEndpoint.class);

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        WebSocketSessionRegistry sessionRegistry = SpringContextHolder.getBean(WebSocketSessionRegistry.class);
        SessionContext context = new DefaultSessionContext(session);
        applySessionConfig(session);
        sessionRegistry.register(session, context);
        session.addMessageHandler(PongMessage.class, message -> sessionRegistry.refreshActivity(session.getId()));
        session.addMessageHandler(String.class, message -> handleMessage(message, session));
        log.info("Session {} 已连接, 在线sessions总数: {}", session.getId(), sessionRegistry.getOnlineSessionCount());
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        if (session == null) {
            return;
        }

        WebSocketSessionCleaner sessionCleaner = SpringContextHolder.getBean(WebSocketSessionCleaner.class);
        sessionCleaner.cleanup(session.getId());
        WebSocketSessionRegistry sessionRegistry = SpringContextHolder.getBean(WebSocketSessionRegistry.class);
        log.info("Session {} 断开连接, 在线sessions总数: {}", session.getId(), sessionRegistry.getOnlineSessionCount());
    }

    private void handleMessage(String message, Session session) {
        log.info("收到客户端信息：sessionId={}", session.getId());

        WebSocketSessionRegistry sessionRegistry = SpringContextHolder.getBean(WebSocketSessionRegistry.class);
        sessionRegistry.refreshActivity(session.getId());
        if (WebSocketHeartbeatManager.FALLBACK_PONG_MESSAGE.equals(message)
                || WebSocketHeartbeatManager.FALLBACK_PING_MESSAGE.equals(message)) {
            return;
        }

        SessionContext context = sessionRegistry.getSessionContext(session.getId());
        if (context == null) {
            log.warn("SessionContext not found, message ignored: sessionId={}", session.getId());
            return;
        }

        SpringContextHolder.getBean(MessageRouter.class).route(message, context);
    }

    @Override
    public void onError(Session session, Throwable error) {
        String sessionId = session == null ? null : session.getId();
        log.error("websocket发生错误！ sessionId={}", sessionId, error);
    }

    private void applySessionConfig(Session session) {
        RealtimeFrameworkProperties properties = SpringContextHolder.getBean(RealtimeFrameworkProperties.class);
        RealtimeFrameworkProperties.WebSocket websocket = properties.getWebsocket();
        session.setMaxTextMessageBufferSize(websocket.getMaxTextMessageBufferSize());
        session.setMaxBinaryMessageBufferSize(websocket.getMaxBinaryMessageBufferSize());
        session.setMaxIdleTimeout(websocket.getMaxSessionIdleTimeout());
    }
}
