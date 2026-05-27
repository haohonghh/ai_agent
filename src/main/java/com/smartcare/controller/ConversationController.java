package com.smartcare.controller;

import com.smartcare.dto.ApiResponse;
import com.smartcare.dto.CreateConversationRequest;
import com.smartcare.model.entity.Conversation;
import com.smartcare.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    public ApiResponse<Map<String, Object>> createConversation(@RequestBody CreateConversationRequest request) {
        Conversation conversation = conversationService.createConversation(
                request.getSource(),
                request.getPriority(),
                null
        );
        Map<String, Object> data = new HashMap<>();
        data.put("conversationId", conversation.getId());
        data.put("status", conversation.getStatus());
        data.put("createdAt", conversation.getCreatedAt().toString());
        return ApiResponse.success(data);
    }

    @GetMapping("/{conversationId}")
    public ApiResponse<Conversation> getConversation(@PathVariable UUID conversationId) {
        return conversationService.getConversationById(conversationId)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(40401, "会话不存在"));
    }

    @GetMapping
    public ApiResponse<Page<Conversation>> getConversations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long agentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Conversation> conversations = conversationService.getConversations(
                status, agentId, PageRequest.of(page, size));
        return ApiResponse.success(conversations);
    }

    @PostMapping("/{conversationId}/close")
    public ApiResponse<Void> closeConversation(@PathVariable UUID conversationId) {
        return conversationService.closeConversation(conversationId)
                .map(c -> ApiResponse.<Void>success())
                .orElse(ApiResponse.error(40401, "会话不存在"));
    }

    @GetMapping("/agent/queue")
    public ApiResponse<List<Map<String, Object>>> getAgentQueue() {
        List<Conversation> waiting = conversationService.getWaitingConversations();
        List<Map<String, Object>> result = waiting.stream().map(conv -> {
            Map<String, Object> item = new HashMap<>();
            item.put("conversationId", conv.getId());
            item.put("customerName", "Customer-" + conv.getCustomerId());
            item.put("waitingTime", java.time.Duration.between(conv.getCreatedAt(), java.time.LocalDateTime.now()).getSeconds());
            item.put("priority", conv.getPriority());
            item.put("lastMessage", "");
            return item;
        }).collect(Collectors.toList());
        return ApiResponse.success(result);
    }
}