package com.smartcare.controller;

import com.smartcare.dto.ApiResponse;
import com.smartcare.dto.SendMessageRequest;
import com.smartcare.model.entity.Message;
import com.smartcare.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/conversations/{conversationId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ApiResponse<Map<String, Object>> sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        Message message = messageService.sendMessage(
                conversationId,
                1L,
                "customer",
                request.getMessageType(),
                request.getContent()
        );
        Map<String, Object> data = new HashMap<>();
        data.put("messageId", message.getId());
        data.put("conversationId", message.getConversationId());
        data.put("senderId", message.getSenderId());
        data.put("senderType", message.getSenderType());
        data.put("messageType", message.getMessageType());
        data.put("content", message.getContent());
        data.put("createdAt", message.getCreatedAt().toString());
        return ApiResponse.success(data);
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(required = false) String before,
            @RequestParam(defaultValue = "50") int limit) {
        List<Message> messages;
        if (before != null) {
            messages = messageService.getMessagesBefore(
                    conversationId,
                    java.time.LocalDateTime.parse(before.replace("Z", "")),
                    limit
            );
        } else {
            messages = messageService.getConversationMessages(conversationId);
        }
        List<Map<String, Object>> result = messages.stream().map(m -> {
            Map<String, Object> item = new HashMap<>();
            item.put("messageId", m.getId());
            item.put("senderType", m.getSenderType());
            item.put("messageType", m.getMessageType());
            item.put("content", m.getContent());
            item.put("createdAt", m.getCreatedAt().toString());
            return item;
        }).collect(Collectors.toList());
        return ApiResponse.success(result);
    }
}