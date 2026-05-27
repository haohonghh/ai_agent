# SmartCare 智能客服系统 - 数据库设计文档

## 1. 概述

本文档描述智能客服系统的数据库表结构设计，基于 PostgreSQL 15 集群。

### 1.1 技术方案

- **消息通道**: WebSocket + Redis Pub/Sub
- **FAQ检索**: Elasticsearch 8.x + 语义匹配双层FAQ
- **会话存储**: Redis Cluster

## 2. 表结构设计

### 2.1 用户表 (users)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 用户ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| password_hash | VARCHAR(255) | NOT NULL | 密码哈希 |
| email | VARCHAR(100) | UNIQUE | 邮箱 |
| phone | VARCHAR(20) | | 手机号 |
| role | VARCHAR(20) | NOT NULL DEFAULT 'customer' | 角色: customer/agent/admin |
| status | VARCHAR(20) | NOT NULL DEFAULT 'active' | 状态: active/inactive/blocked |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | 更新时间 |

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'customer',
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
```

### 2.2 客服会话表 (conversations)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | UUID | PRIMARY KEY DEFAULT gen_random_uuid() | 会话ID |
| customer_id | BIGINT | REFERENCES users(id) | 客户ID |
| agent_id | BIGINT | REFERENCES users(id) | 客服ID |
| status | VARCHAR(20) | NOT NULL DEFAULT 'waiting' | 状态: waiting/active/closed |
| priority | INTEGER | NOT NULL DEFAULT 0 | 优先级 (0-10) |
| source | VARCHAR(20) | NOT NULL DEFAULT 'web' | 来源: web/app/wechat |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | 创建时间 |
| closed_at | TIMESTAMP | | 关闭时间 |

```sql
CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id BIGINT REFERENCES users(id),
    agent_id BIGINT REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'waiting',
    priority INTEGER NOT NULL DEFAULT 0,
    source VARCHAR(20) NOT NULL DEFAULT 'web',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    closed_at TIMESTAMP
);

CREATE INDEX idx_conversations_customer ON conversations(customer_id);
CREATE INDEX idx_conversations_agent ON conversations(agent_id);
CREATE INDEX idx_conversations_status ON conversations(status);
CREATE INDEX idx_conversations_created ON conversations(created_at);
```

### 2.3 消息表 (messages)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 消息ID |
| conversation_id | UUID | REFERENCES conversations(id) | 会话ID |
| sender_id | BIGINT | REFERENCES users(id) | 发送者ID |
| sender_type | VARCHAR(20) | NOT NULL | 发送者类型: customer/agent/bot |
| message_type | VARCHAR(20) | NOT NULL DEFAULT 'text' | 消息类型: text/image/file/audio |
| content | TEXT | NOT NULL | 消息内容 |
| metadata | JSONB | | 扩展字段 |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | 创建时间 |

```sql
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id UUID REFERENCES conversations(id),
    sender_id BIGINT REFERENCES users(id),
    sender_type VARCHAR(20) NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'text',
    content TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_created ON messages(created_at);
```

### 2.4 FAQ知识库表 (faq_knowledge)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | FAQ ID |
| category_id | BIGINT | REFERENCES faq_categories(id) | 分类ID |
| question | TEXT | NOT NULL | 问题 |
| question_embedding | vector(1536) | | 问题向量 (用于语义匹配) |
| answer | TEXT | NOT NULL | 答案 |
| keywords | TEXT[] | | 关键词数组 |
| view_count | INTEGER | NOT NULL DEFAULT 0 | 浏览次数 |
| helpful_count | INTEGER | NOT NULL DEFAULT 0 | 有帮助次数 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'active' | 状态 |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | 更新时间 |

```sql
CREATE TABLE faq_knowledge (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT REFERENCES faq_categories(id),
    question TEXT NOT NULL,
    question_embedding vector(1536),
    answer TEXT NOT NULL,
    keywords TEXT[],
    view_count INTEGER NOT NULL DEFAULT 0,
    helpful_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_faq_category ON faq_knowledge(category_id);
CREATE INDEX idx_faq_status ON faq_knowledge(status);
CREATE INDEX idx_faq_keywords ON faq_knowledge USING GIN(keywords);
CREATE INDEX idx_faq_embedding ON faq_knowledge USING IVFFLAT(question_embedding vector_cosine_ops);
```

### 2.5 FAQ分类表 (faq_categories)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 分类ID |
| name | VARCHAR(100) | NOT NULL | 分类名称 |
| parent_id | BIGINT | REFERENCES faq_categories(id) | 父分类ID |
| sort_order | INTEGER | NOT NULL DEFAULT 0 | 排序 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'active' | 状态 |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | 创建时间 |

```sql
CREATE TABLE faq_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT REFERENCES faq_categories(id),
    sort_order INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_faq_cat_parent ON faq_categories(parent_id);
```

### 2.6 客服评价表 (conversation_ratings)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 评价ID |
| conversation_id | UUID | REFERENCES conversations(id) | 会话ID |
| rating | INTEGER | NOT NULL CHECK (rating >= 1 AND rating <= 5) | 评分 (1-5) |
| tags | TEXT[] | | 标签 |
| comment | TEXT | | 评价内容 |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | 创建时间 |

```sql
CREATE TABLE conversation_ratings (
    id BIGSERIAL PRIMARY KEY,
    conversation_id UUID REFERENCES conversations(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    tags TEXT[],
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rating_conversation ON conversation_ratings(conversation_id);
```

### 2.7 系统配置表 (system_config)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 配置ID |
| config_key | VARCHAR(100) | UNIQUE NOT NULL | 配置键 |
| config_value | TEXT | NOT NULL | 配置值 |
| description | VARCHAR(255) | | 说明 |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | 更新时间 |

```sql
CREATE TABLE system_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    description VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

## 3. 索引优化策略

1. **高频查询索引**:
   - `idx_conversations_status_created` - 按状态和时间查询待处理会话
   - `idx_messages_conversation_created` - 按会话查询消息

2. **全文搜索**:
   - FAQ问题使用GIN索引支持关键词搜索
   - 向量索引使用IVFFLAT加速语义匹配

3. **分区策略**:
   - `messages`表按月分区，避免大数据量影响查询性能
   - `conversations`表按年归档

## 4. 数据库连接池配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

## 5. 集群部署建议

- **主从复制**: 1主2从架构
- **读写分离**: 写操作走主库，统计分析走从库
- **定期归档**: 历史数据迁移到归档表