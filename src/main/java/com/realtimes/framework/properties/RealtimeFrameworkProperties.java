package com.realtimes.framework.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "realtime")
public class RealtimeFrameworkProperties {

    private boolean enabled = true;

    private WebSocket websocket = new WebSocket();

    private EventBus eventBus = new EventBus();

    private Storage storage = new Storage();

    private ThreadPool threadPool = new ThreadPool();

    @Data
    public static class WebSocket {
        private boolean enabled = true;
        private String path = "/websocket/subscribe";
        private int maxTextMessageBufferSize = 8192;
        private int maxBinaryMessageBufferSize = 8192;
        private long maxSessionIdleTimeout = 300000;
    }

    @Data
    public static class EventBus {
        private String type = "memory";
        private boolean async = true;
    }

    @Data
    public static class Storage {
        private String type = "memory";
        private long subscriptionTimeout = 300000;
        private int maxSubscriptionCount = 10000;
    }

    @Data
    public static class ThreadPool {
        private int corePoolSize = 5;
        private int maxPoolSize = 20;
        private int queueCapacity = 100;
        private int keepAliveSeconds = 60;
    }
}

