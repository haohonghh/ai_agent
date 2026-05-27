package com.multica.customer.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 聊天消息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    private String messageId;
    private String sessionId;
    private String senderId;
    private SenderType senderType;
    private MessageType messageType;
    private String content;
    private String attachmentUrl;
    private LocalDateTime timestamp;
    private boolean read;

    public enum SenderType {
        USER,
        BOT,
        AGENT
    }

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        SYSTEM
    }
}
