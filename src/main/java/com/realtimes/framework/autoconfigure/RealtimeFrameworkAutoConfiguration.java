package com.realtimes.framework.autoconfigure;

import com.realtimes.framework.api.event.Event;
import com.realtimes.framework.api.event.EventBus;
import com.realtimes.framework.api.event.EventListener;
import com.realtimes.framework.api.message.MessageProcessor;
import com.realtimes.framework.api.message.MessageRouter;
import com.realtimes.framework.api.message.MessageSender;
import com.realtimes.framework.api.session.SessionStorage;
import com.realtimes.framework.api.subscription.SubscriptionManager;
import com.realtimes.framework.api.subscription.SubscriptionStorage;
import com.realtimes.framework.internal.event.MemoryEventBus;
import com.realtimes.framework.internal.message.DefaultMessageRouter;
import com.realtimes.framework.internal.message.DefaultMessageSender;
import com.realtimes.framework.internal.storage.MemorySessionStorage;
import com.realtimes.framework.internal.storage.MemorySubscriptionStorage;
import com.realtimes.framework.internal.subscription.DefaultSubscriptionManager;
import com.realtimes.framework.internal.support.SpringContextHolder;
import com.realtimes.framework.internal.websocket.DefaultWebSocketEndpoint;
import com.realtimes.framework.internal.websocket.MemoryWebSocketSessionRegistry;
import com.realtimes.framework.internal.websocket.WebSocketSessionRegistry;
import com.realtimes.framework.properties.RealtimeFrameworkProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@AutoConfiguration
@EnableConfigurationProperties(RealtimeFrameworkProperties.class)
@ConditionalOnProperty(prefix = "realtime", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RealtimeFrameworkAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RealtimeFrameworkAutoConfiguration.class);

    @Bean("realtimeFrameworkExecutor")
    @ConditionalOnMissingBean(name = "realtimeFrameworkExecutor")
    public ThreadPoolTaskExecutor realtimeFrameworkExecutor(RealtimeFrameworkProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getThreadPool().getCorePoolSize());
        executor.setMaxPoolSize(properties.getThreadPool().getMaxPoolSize());
        executor.setQueueCapacity(properties.getThreadPool().getQueueCapacity());
        executor.setKeepAliveSeconds(properties.getThreadPool().getKeepAliveSeconds());
        executor.setThreadNamePrefix("realtime-task-");
        executor.initialize();
        return executor;
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "realtime.storage", name = "type", havingValue = "memory", matchIfMissing = true)
    public SubscriptionStorage realtimeSubscriptionStorage() {
        return new MemorySubscriptionStorage();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "realtime.storage", name = "type", havingValue = "memory", matchIfMissing = true)
    public SessionStorage realtimeSessionStorage() {
        return new MemorySessionStorage();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "realtime.eventbus", name = "type", havingValue = "memory", matchIfMissing = true)
    public EventBus<Event, Object> realtimeEventBus(ThreadPoolTaskExecutor realtimeFrameworkExecutor,
                                                                    ObjectProvider<EventListener<?, ?>> listeners) {
        MemoryEventBus<Event, Object> eventBus = new MemoryEventBus<>(realtimeFrameworkExecutor);
        eventBus.start();
        listeners.stream().forEach(listener -> registerEventListener(eventBus, listener));
        return eventBus;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerEventListener(MemoryEventBus<Event, Object> eventBus,
                                       EventListener<?, ?> listener) {
        try {
            eventBus.register((EventListener) listener);
            log.info("Registered realtime event listener: {}", listener.getName());
        } catch (Exception e) {
            log.error("Failed to register realtime event listener: {}", listener.getName(), e);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public SubscriptionManager realtimeSubscriptionManager(SubscriptionStorage subscriptionStorage,
                                                                  SessionStorage sessionStorage) {
        return new DefaultSubscriptionManager(subscriptionStorage, sessionStorage);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebSocketSessionRegistry realtimeWebSocketSessionRegistry(SessionStorage sessionStorage) {
        return new MemoryWebSocketSessionRegistry(sessionStorage);
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageRouter realtimeMessageRouter(ObjectProvider<MessageProcessor<?>> processors) {
        DefaultMessageRouter router = new DefaultMessageRouter();
        processors.stream().forEach(router::registerProcessor);
        return router;
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageSender realtimeMessageSender(WebSocketSessionRegistry sessionRegistry,
                                                       SubscriptionManager subscriptionManager) {
        return new DefaultMessageSender(sessionRegistry, subscriptionManager);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "realtime.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DefaultWebSocketEndpoint defaultRealtimeWebSocketEndpoint() {
        return new DefaultWebSocketEndpoint();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "realtime.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}

