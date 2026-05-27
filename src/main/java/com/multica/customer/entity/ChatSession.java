package com.multica.customer.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 聊天会话实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    private String sessionId;
    private String userId;
    private String userName;
    private String agentId;
    private SessionStatus status;
    private SessionSource source;
    private LocalDateTime createdAt;
    private LocalDateTime connectedAt;
    private LocalDateTime endedAt;
    private Long waitTimeSeconds;

    public enum SessionStatus {
        WAITING,      // 等待接入
        QUEUED,       // 排队中
        CONNECTED,    // 已连接坐席
        TRANSFERRED,  // 转接中
        CLOSED        // 已结束
    }

    public enum SessionSource {
        WEB,          // 网页
        WECHAT,       // 微信
        APP           // APP
    }
}
