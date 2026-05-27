package com.multica.customer.websocket;

import com.multica.customer.dto.WebSocketMessage;
import com.multica.customer.entity.ChatMessage;
import com.multica.customer.service.MessageService;
import com.multica.customer.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * WebSocket消息处理器
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketHandler {

    private final SessionService sessionService;
    private final MessageService messageService;

    /**
     * 处理客户端消息
     */
    @MessageMapping("/chat.send")
    @SendTo("/topic/session/{sessionId}")
    public WebSocketMessage handleMessage(
            @Payload WebSocketMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        log.debug("收到WebSocket消息: {}", message);

        String sessionId = message.getSessionId();
        if (sessionId == null) {
            log.warn("sessionId为空，忽略消息");
            return null;
        }

        // 发送消息
        ChatMessage.SenderType senderType = ChatMessage.SenderType.valueOf(
                message.getSenderType() != null ? message.getSenderType() : "USER"
        );

        messageService.sendMessage(
                sessionId,
                message.getSenderId(),
                senderType,
                message.getContent()
        );

        return message;
    }

    /**
     * 加入会话
     */
    @MessageMapping("/chat.join")
    @SendTo("/topic/session/{sessionId}")
    public WebSocketMessage handleJoin(
            @Payload WebSocketMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("用户加入会话: {}", message.getSessionId());

        WebSocketMessage response = WebSocketMessage.builder()
                .type("system")
                .sessionId(message.getSessionId())
                .content("用户 " + message.getSenderId() + " 已加入会话")
                .timestamp(System.currentTimeMillis())
                .build();

        return response;
    }

    /**
     * 离开会话
     */
    @MessageMapping("/chat.leave")
    @SendTo("/topic/session/{sessionId}")
    public WebSocketMessage handleLeave(
            @Payload WebSocketMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("用户离开会话: {}", message.getSessionId());

        WebSocketMessage response = WebSocketMessage.builder()
                .type("system")
                .sessionId(message.getSessionId())
                .content("用户 " + message.getSenderId() + " 已离开会话")
                .timestamp(System.currentTimeMillis())
                .build();

        return response;
    }
}
