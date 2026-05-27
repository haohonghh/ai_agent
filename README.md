# Multica Customer Service Backend

智能客服系统核心后端服务 (Spring Boot 3.2)

## 技术栈

- **框架**: Spring Boot 3.2.0
- **Java**: 17
- **消息通信**: WebSocket (STOMP) + Redis Pub/Sub
- **搜索**: Elasticsearch 8.x
- **构建**: Maven

## 模块架构

```
com.multica.customer
├── config/          # 配置类
│   ├── WebSocketConfig      # WebSocket配置
│   ├── RedisConfig          # Redis配置
│   ├── ElasticsearchConfig  # ES配置
│   └── WebMvcConfig         # CORS配置
├── controller/      # REST控制器
│   ├── SessionController     # 会话管理
│   ├── BotController        # 机器人FAQ
│   ├── WorkOrderController  # 工单管理
│   ├── MessageController    # 消息管理
│   └── QueueController      # 队列管理
├── service/         # 业务逻辑
│   ├── SessionService      # 会话服务
│   ├── AgentService        # 坐席服务
│   ├── QueueService        # 队列服务
│   ├── BotService          # 机器人服务
│   ├── WorkOrderService    # 工单服务
│   └── MessageService      # 消息服务
├── entity/          # 实体类
├── dto/             # 数据传输对象
├── repository/      # ES仓储
└── websocket/       # WebSocket处理器
```

## 核心功能

### P0 功能

1. **机器人自动问答**
   - FAQ检索 (ES)
   - 语义匹配
   - 意图识别
   - 自动回复

2. **人工转接接入**
   - WebSocket实时通信
   - 坐席分配 (最少连接数算法)
   - 会话状态流转

3. **排队队列管理**
   - 优先级排队
   - FIFO分配
   - 超时处理

### P1 功能

1. **满意度评价**
2. **自动转接规则**

## API接口

### 会话管理

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/sessions` | 创建新会话 |
| GET | `/api/sessions/{sessionId}` | 获取会话详情 |
| POST | `/api/sessions/{sessionId}/close` | 结束会话 |
| POST | `/api/sessions/{sessionId}/transfer` | 转接会话 |
| GET | `/api/sessions/agents` | 获取在线坐席 |

### 机器人服务

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/bot/search` | 检索FAQ |
| GET | `/api/bot/reply` | 获取自动回复 |
| GET | `/api/bot/intent` | 意图识别 |

### 消息服务

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/messages/session/{sessionId}` | 获取消息历史 |
| POST | `/api/messages/session/{sessionId}` | 发送消息 |

### 工单服务

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/work-orders/{workOrderId}` | 获取工单详情 |
| POST | `/api/work-orders/{workOrderId}/satisfaction` | 提交满意度 |
| POST | `/api/work-orders/{workOrderId}/close` | 关闭工单 |

### 队列管理

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/queue/size` | 获取排队人数 |
| GET | `/api/queue/position` | 获取排队位置 |

## WebSocket

端点: `/ws`
STOMP主题: `/topic/session/{sessionId}`

## 快速启动

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 测试API
curl -X POST http://localhost:8080/api/sessions \
  -H "Content-Type: application/json" \
  -d '{"userId":"user001","userName":"张三","source":"WEB"}'
```

## 配置说明

主要配置项 (`application.yml`):

```yaml
server:
  port: 8080

spring:
  redis:
    host: localhost
    port: 6379
  elasticsearch:
    uris: http://localhost:9200
```
