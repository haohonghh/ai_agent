package com.multica.customer.controller;

import com.multica.customer.dto.ApiResponse;
import com.multica.customer.entity.ChatMessage;
import com.multica.customer.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取会话消息历史
     */
    @GetMapping("/session/{sessionId}")
    public ApiResponse<List<ChatMessage>> getSessionMessages(@PathVariable String sessionId) {
        List<ChatMessage> messages = messageService.getSessionMessages(sessionId);
        return ApiResponse.success(messages);
    }

    /**
     * 发送消息
     */
    @PostMapping("/session/{sessionId}")
    public ApiResponse<ChatMessage> sendMessage(
            @PathVariable String sessionId,
            @RequestParam String senderId,
            @RequestParam String senderType,
            @RequestBody String content) {
        try {
            ChatMessage.SenderType type = ChatMessage.SenderType.valueOf(senderType.toUpperCase());
            ChatMessage message = messageService.sendMessage(sessionId, senderId, type, content);
            return ApiResponse.success(message);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, "无效的发送者类型");
        }
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/{messageId}/read")
    public ApiResponse<Void> markAsRead(@PathVariable String messageId) {
        messageService.markAsRead(messageId);
        return ApiResponse.success("已标记", null);
    }
}
