# SmartCare 智能客服系统 - API接口文档

## 1. 概述

本文档描述智能客服系统的RESTful API接口规范。

### 1.1 Base URL

```
http://localhost:8080/api/v1
```

### 1.2 认证方式

采用 JWT Token 认证，Token 放在 Header 中:
```
Authorization: Bearer <token>
```

### 1.3 通用响应格式

**成功响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```

**错误响应:**
```json
{
  "code": 40001,
  "message": "参数错误",
  "data": null
}
```

### 1.4 错误码定义

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 40001 | 参数错误 |
| 40002 | 缺少必填参数 |
| 40101 | 未登录 |
| 40102 | Token过期 |
| 40301 | 无权限 |
| 40401 | 资源不存在 |
| 50001 | 服务器内部错误 |

## 2. 用户认证接口

### 2.1 用户登录

**POST** `/auth/login`

**请求参数:**
```json
{
  "username": "string",
  "password": "string"
}
```

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "username": "agent01",
      "role": "agent"
    }
  }
}
```

### 2.2 用户登出

**POST** `/auth/logout`

**响应:**
```json
{
  "code": 0,
  "message": "success"
}
```

## 3. 会话接口

### 3.1 创建会话

**POST** `/conversations`

**请求参数:**
```json
{
  "source": "web",
  "priority": 0,
  "customerName": "string"
}
```

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "conversationId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "waiting",
    "createdAt": "2026-05-27T08:00:00Z"
  }
}
```

### 3.2 获取会话详情

**GET** `/conversations/{conversationId}`

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "customerId": 1,
    "agentId": 2,
    "status": "active",
    "priority": 0,
    "source": "web",
    "createdAt": "2026-05-27T08:00:00Z",
    "closedAt": null
  }
}
```

### 3.3 关闭会话

**POST** `/conversations/{conversationId}/close`

**响应:**
```json
{
  "code": 0,
  "message": "success"
}
```

### 3.4 获取会话列表

**GET** `/conversations`

**查询参数:**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| status | string | 筛选状态: waiting/active/closed |
| agentId | long | 筛选客服ID |
| page | int | 页码 (默认0) |
| size | int | 每页数量 (默认20) |

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 5,
    "page": 0,
    "size": 20
  }
}
```

## 4. 消息接口

### 4.1 发送消息

**POST** `/conversations/{conversationId}/messages`

**请求参数:**
```json
{
  "messageType": "text",
  "content": "您好，有什么可以帮您？"
}
```

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "messageId": 12345,
    "conversationId": "550e8400-e29b-41d4-a716-446655440000",
    "senderId": 2,
    "senderType": "agent",
    "messageType": "text",
    "content": "您好，有什么可以帮您？",
    "createdAt": "2026-05-27T08:01:00Z"
  }
}
```

### 4.2 获取历史消息

**GET** `/conversations/{conversationId}/messages`

**查询参数:**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| before | timestamp | 获取此时间之前的消息 |
| limit | int | 每次获取数量 (默认50) |

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "messageId": 12345,
      "senderType": "customer",
      "messageType": "text",
      "content": "我想咨询产品报价",
      "createdAt": "2026-05-27T08:00:30Z"
    }
  ]
}
```

## 5. FAQ接口

### 5.1 关键词搜索FAQ

**GET** `/faqs/search`

**查询参数:**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| keyword | string | 搜索关键词 |
| categoryId | long | 分类ID (可选) |
| page | int | 页码 |
| size | int | 每页数量 |

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "content": [
      {
        "id": 1,
        "categoryName": "产品咨询",
        "question": "产品报价是多少?",
        "answer": "我们的产品分为基础版、专业版和企业版，价格从...",
        "viewCount": 100
      }
    ],
    "totalElements": 50,
    "page": 0,
    "size": 10
  }
}
```

### 5.2 语义搜索FAQ

**POST** `/faqs/semantic-search`

**请求参数:**
```json
{
  "query": "我想了解产品的价格",
  "topK": 5
}
```

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "question": "产品报价是多少?",
      "answer": "我们的产品分为基础版、专业版和企业版，价格从...",
      "score": 0.95
    }
  ]
}
```

### 5.3 获取FAQ详情

**GET** `/faqs/{faqId}`

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "categoryId": 1,
    "categoryName": "产品咨询",
    "question": "产品报价是多少?",
    "answer": "我们的产品分为基础版、专业版和企业版...",
    "keywords": ["价格", "报价", "收费"],
    "viewCount": 100,
    "helpfulCount": 50
  }
}
```

### 5.4 评价FAQ有帮助

**POST** `/faqs/{faqId}/rate`

**请求参数:**
```json
{
  "helpful": true
}
```

**响应:**
```json
{
  "code": 0,
  "message": "success"
}
```

## 6. WebSocket 实时消息接口

### 6.1 连接地址

```
ws://localhost:8080/ws/chat?token=<jwt_token>
```

### 6.2 客户端发送消息

```json
{
  "type": "message",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "content": "您好"
}
```

### 6.3 服务端推送消息

```json
{
  "type": "message",
  "data": {
    "messageId": 12345,
    "conversationId": "550e8400-e29b-41d4-a716-446655440000",
    "senderType": "customer",
    "content": "您好",
    "createdAt": "2026-05-27T08:00:30Z"
  }
}
```

### 6.4 消息类型

| type | 说明 |
|------|------|
| message | 普通消息 |
| typing | 用户正在输入 |
| online_status | 在线状态变更 |
| conversation_update | 会话状态变更 |

## 7. 客服工作台接口

### 7.1 获取待处理会话

**GET** `/agent/queue`

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "conversationId": "550e8400-e29b-41d4-a716-446655440000",
      "customerName": "张三",
      "waitingTime": 120,
      "priority": 5,
      "lastMessage": "我想咨询产品报价"
    }
  ]
}
```

### 7.2 接受会话

**POST** `/agent/accept`

**请求参数:**
```json
{
  "conversationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**响应:**
```json
{
  "code": 0,
  "message": "success"
}
```

## 8. 统计接口

### 8.1 获取客服统计

**GET** `/stats/agent`

**查询参数:**
| 参数名 | 类型 | 说明 |
|--------|------|------|
| startDate | date | 开始日期 (YYYY-MM-DD) |
| endDate | date | 结束日期 |

**响应:**
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "totalConversations": 150,
    "avgResponseTime": 30.5,
    "avgResolutionTime": 300.0,
    "satisfactionRate": 4.5
  }
}
```