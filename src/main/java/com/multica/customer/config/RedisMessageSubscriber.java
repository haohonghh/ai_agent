package com.multica.customer.config;

import com.multica.customer.dto.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis消息订阅器 - 跨实例消息分发
 */
@Slf4j
@Component
public class RedisMessageSubscriber {

    private final SimpMessagingTemplate messagingTemplate;

    public RedisMessageSubscriber(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 接收Redis消息并转发到WebSocket
     */
    public void onMessage(WebSocketMessage message, String channel) {
        if (message == null || message.getSessionId() == null) {
            return;
        }

        log.debug("收到Redis消息: channel={}, session={}", channel, message.getSessionId());

        // 转发到WebSocket主题
        messagingTemplate.convertAndSend("/topic/session/" + message.getSessionId(), message);
    }
}
