package com.smartcare.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketHandler {

    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/chat/{conversationId}")
    public void handleMessage(@DestinationVariable String conversationId, Map<String, Object> payload) {
        String type = (String) payload.get("type");
        log.info("WebSocket message received: type={}, conversationId={}", type, conversationId);

        if ("message".equals(type)) {
            String content = (String) payload.get("content");
            Map<String, Object> response = Map.of(
                    "type", "message",
                    "conversationId", conversationId,
                    "content", content,
                    "createdAt", java.time.LocalDateTime.now().toString()
            );
            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, response);
        } else if ("typing".equals(type)) {
            Map<String, Object> response = Map.of(
                    "type", "typing",
                    "conversationId", conversationId
            );
            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, response);
        }
    }
}