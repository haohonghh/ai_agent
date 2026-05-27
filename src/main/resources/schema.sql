-- --------------------------------------------------------
-- 智能客服系统 - 数据库初始化脚本
-- --------------------------------------------------------

-- 创建数据库
CREATE DATABASE IF NOT EXISTS customer_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE customer_service;

-- --------------------------------------------------------
-- 表1: 坐席表 (agents)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS agents (
    agent_id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL COMMENT '坐席名称',
    status ENUM('ONLINE', 'BUSY', 'AWAY', 'OFFLINE') DEFAULT 'OFFLINE' COMMENT '状态',
    skills VARCHAR(512) DEFAULT 'general' COMMENT '技能标签，逗号分隔',
    current_load INT DEFAULT 0 COMMENT '当前负载会话数',
    max_load INT DEFAULT 10 COMMENT '最大负载会话数',
    last_active_at DATETIME COMMENT '最后活跃时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_last_active (last_active_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='坐席表';

-- --------------------------------------------------------
-- 表2: 会话表 (chat_sessions)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_sessions (
    session_id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    user_name VARCHAR(128) COMMENT '用户名',
    agent_id VARCHAR(64) COMMENT '分配的坐席ID',
    status ENUM('WAITING', 'QUEUED', 'CONNECTED', 'TRANSFERRED', 'CLOSED') DEFAULT 'WAITING' COMMENT '状态',
    source ENUM('WEB', 'WECHAT', 'APP') DEFAULT 'WEB' COMMENT '来源渠道',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    connected_at DATETIME COMMENT '连接时间',
    ended_at DATETIME COMMENT '结束时间',
    wait_time_seconds INT COMMENT '等待时长(秒)',
    INDEX idx_user_id (user_id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (agent_id) REFERENCES agents(agent_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- --------------------------------------------------------
-- 表3: 消息表 (chat_messages)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_messages (
    message_id VARCHAR(64) PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    sender_id VARCHAR(64) NOT NULL COMMENT '发送者ID',
    sender_type ENUM('USER', 'BOT', 'AGENT') NOT NULL COMMENT '发送者类型',
    message_type ENUM('TEXT', 'IMAGE', 'FILE', 'SYSTEM') DEFAULT 'TEXT' COMMENT '消息类型',
    content TEXT COMMENT '消息内容',
    attachment_url VARCHAR(512) COMMENT '附件URL',
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    INDEX idx_session_id (session_id),
    INDEX idx_timestamp (timestamp),
    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- --------------------------------------------------------
-- 表4: 工单表 (work_orders)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS work_orders (
    work_order_id VARCHAR(64) PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL COMMENT '关联会话ID',
    agent_id VARCHAR(64) COMMENT '处理坐席ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED') DEFAULT 'OPEN' COMMENT '状态',
    satisfaction_score TINYINT COMMENT '满意度评分(1-5)',
    satisfaction_comment TEXT COMMENT '满意度评价内容',
    summary VARCHAR(512) COMMENT '会话摘要',
    transcript TEXT COMMENT '会话记录JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    closed_at DATETIME COMMENT '关闭时间',
    INDEX idx_session_id (session_id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id) ON DELETE CASCADE,
    FOREIGN KEY (agent_id) REFERENCES agents(agent_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- --------------------------------------------------------
-- 表5: FAQ表 (faqs)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS faqs (
    id VARCHAR(64) PRIMARY KEY,
    question VARCHAR(512) NOT NULL COMMENT '问题',
    answer TEXT NOT NULL COMMENT '答案',
    category VARCHAR(128) COMMENT '分类',
    keywords VARCHAR(512) COMMENT '关键词，逗号分隔',
    hit_count INT DEFAULT 0 COMMENT '命中次数',
    confidence_threshold DECIMAL(3,2) DEFAULT 0.70 COMMENT '置信度阈值',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_hit_count (hit_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='FAQ表';

-- --------------------------------------------------------
-- 初始化坐席数据
-- --------------------------------------------------------
INSERT INTO agents (agent_id, name, status, skills, current_load, max_load) VALUES
('agent_1', '坐席小王', 'ONLINE', 'general', 0, 10),
('agent_2', '坐席小李', 'ONLINE', 'general', 0, 10),
('agent_3', '坐席小张', 'ONLINE', 'general,vip', 0, 10);

-- --------------------------------------------------------
-- 初始化FAQ数据
-- --------------------------------------------------------
INSERT INTO faqs (id, question, answer, category, keywords) VALUES
('faq_001', '如何重置密码', '您可以通过以下步骤重置密码：1. 点击登录页的"忘记密码"链接；2. 输入注册邮箱；3. 查收邮件并点击验证链接；4. 设置新密码', '账户', '密码,重置,忘记'),
('faq_002', '如何联系人工客服', '您可以输入"转人工"或"人工客服"来连接我们的坐席团队，我们的工作时间是周一至周五 9:00-18:00。', '服务', '人工,客服,转接'),
('faq_003', '业务办理时间', '我们的业务办理时间为：周一至周五 9:00-18:00，周六 10:00-16:00，周日休息。', '服务', '时间,营业,办公'),
('faq_004', '如何申请退款', '退款申请可通过以下渠道提交：1. 在"我的订单"中找到对应订单；2. 点击"申请退款"按钮；3. 填写退款原因；4. 提交后预计3-7个工作日到账。', '账户', '退款,申请,订单'),
('faq_005', '产品如何使用', '感谢您的咨询，请访问我们的帮助中心获取详细的产品使用教程：https://help.example.com', '产品', '使用,教程,帮助');
