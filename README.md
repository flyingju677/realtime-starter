# realtime-starter

`realtime-starter` 是实时数据处理框架的基础设施工程，定位为可复用的 Spring Boot Starter。它封装 WebSocket 连接、会话管理、订阅管理、消息路由、消息发送、事件总线和内存存储等底层能力，让业务工程只需要面向 API/SPI 扩展处理逻辑。

## 设计思路

框架采用“API 契约 + 默认实现 + 自动装配”的设计。`api` 包定义业务侧可依赖的接口，`internal` 包提供当前默认实现，`autoconfigure` 负责在 Spring Boot 应用启动时按配置装配组件。

默认实现以单机内存模式为主，适合框架验证、轻量业务和本地开发。后续如需接入 Redis、MQ 或分布式会话，应优先通过扩展 `api` 包接口替换默认 Bean，而不是让业务工程直接依赖 `internal` 类。

## 项目结构

```text
src/main/java/com/realtimes/framework
├── api/              # 对外 API/SPI：事件、消息、会话、订阅
├── autoconfigure/    # Spring Boot 自动配置
├── internal/         # 框架内部默认实现
└── properties/       # 配置属性映射

src/main/resources/META-INF/spring
└── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

## 技术栈

- Java 17
- Spring Boot 3.2.4
- Spring Boot AutoConfiguration
- Spring WebSocket
- fastjson2
- Lombok
- JUnit 5

## 关键类说明

- `RealtimeFrameworkAutoConfiguration`：自动配置入口，创建线程池、存储、事件总线、消息路由、消息发送器和 WebSocket 端点。
- `RealtimeFrameworkProperties`：绑定 `realtime.*` 配置项。
- `MessageProcessor`：业务消息处理器 SPI，根据消息类型处理客户端消息。
- `MessageRouter`：消息路由接口，负责找到匹配的处理器。
- `MessageSender`：消息发送接口，支持面向会话或订阅目标推送数据。
- `SubscriptionManager`：订阅关系管理接口。
- `EventBus` / `EventListener`：框架事件发布与监听机制。
- `DefaultWebSocketEndpoint`：默认 WebSocket 连接入口，由自动配置按 `realtime.websocket.path` 动态注册。

## 构建与测试

```bash
mvn -f realtime-starter/pom.xml compile
mvn -f realtime-starter/pom.xml test
```

`compile` 用于静态编译校验，`test` 运行模块测试。修改 starter 代码后，至少执行本模块编译；如果改动 API/SPI，还需要同步验证依赖该 starter 的业务工程。

## 配置项

核心配置前缀为 `realtime`，主要包含：

- `realtime.enabled`：是否启用实时框架。
- `realtime.websocket.enabled`：是否启用 WebSocket。
- `realtime.websocket.path`：WebSocket 订阅路径，默认 `/websocket/subscribe`，可按业务服务配置动态调整。
- `realtime.eventbus.type`：事件总线类型，当前默认 `memory`。
- `realtime.storage.type`：存储类型，当前默认 `memory`。
- `realtime.thread-pool.*`：框架异步线程池参数。

## 开发约束

对外可依赖能力应放在 `api` 包，默认实现放在 `internal` 包。业务工程不得直接引用 `com.realtimes.framework.internal` 下的类。涉及 API/SPI 变更、默认 Bean 替换策略或存储架构变化时，需要人工架构审查。
