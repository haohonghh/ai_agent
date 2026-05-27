package com.multica.customer.service;

import com.multica.customer.dto.WebSocketMessage;
import com.multica.customer.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 消息服务 - 消息收发、Redis Pub/Sub分发
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, WebSocketMessage> redisTemplate;
    private final BotService botService;

    private final Map<String, ChatMessage> messages = new ConcurrentHashMap<>();

    /**
     * 发送消息
     */
    public ChatMessage sendMessage(String sessionId, String senderId,
                                   ChatMessage.SenderType senderType, String content) {
        String messageId = UUID.randomUUID().toString();

        ChatMessage message = ChatMessage.builder()
                .messageId(messageId)
                .sessionId(sessionId)
                .senderId(senderId)
                .senderType(senderType)
                .messageType(ChatMessage.MessageType.TEXT)
                .content(content)
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        messages.put(messageId, message);

        // 通过WebSocket发送
        WebSocketMessage wsMessage = toWebSocketMessage(message);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, wsMessage);

        // 通过Redis Pub/Sub发布
        redisTemplate.convertAndSend("chat:session:" + sessionId, wsMessage);

        // 如果是用户消息，触发机器人自动回复
        if (senderType == ChatMessage.SenderType.USER) {
            processBotResponse(sessionId, senderId, content);
        }

        log.debug("消息发送: {} -> session: {}", messageId, sessionId);
        return message;
    }

    /**
     * 处理机器人自动回复
     */
    private void processBotResponse(String sessionId, String senderId, String content) {
        String botResponse = botService.generateAutoReply(content);

        ChatMessage botMessage = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .sessionId(sessionId)
                .senderId("bot")
                .senderType(ChatMessage.SenderType.BOT)
                .messageType(ChatMessage.MessageType.TEXT)
                .content(botResponse)
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        messages.put(botMessage.getMessageId(), botMessage);

        // 通过WebSocket发送
        WebSocketMessage wsMessage = toWebSocketMessage(botMessage);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, wsMessage);

        // 通过Redis Pub/Sub发布
        redisTemplate.convertAndSend("chat:session:" + sessionId, wsMessage);

        log.debug("机器人回复: {}", botMessage.getMessageId());
    }

    /**
     * 获取会话消息历史
     */
    public List<ChatMessage> getSessionMessages(String sessionId) {
        return messages.values().stream()
                .filter(m -> m.getSessionId().equals(sessionId))
                .sorted((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * 标记消息已读
     */
    public void markAsRead(String messageId) {
        ChatMessage message = messages.get(messageId);
        if (message != null) {
            message.setRead(true);
        }
    }

    private WebSocketMessage toWebSocketMessage(ChatMessage message) {
        return WebSocketMessage.builder()
                .type("message")
                .sessionId(message.getSessionId())
                .content(message.getContent())
                .senderId(message.getSenderId())
                .senderType(message.getSenderType().name())
                .timestamp(message.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();
    }
}
