package com.multica.customer.controller;

import com.multica.customer.dto.ApiResponse;
import com.multica.customer.dto.CreateSessionRequest;
import com.multica.customer.dto.SessionResponse;
import com.multica.customer.entity.ChatSession;
import com.multica.customer.entity.Agent;
import com.multica.customer.service.SessionService;
import com.multica.customer.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会话控制器
 */
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final WorkOrderService workOrderService;

    /**
     * 创建新会话
     */
    @PostMapping
    public ApiResponse<SessionResponse> createSession(@RequestBody CreateSessionRequest request) {
        SessionResponse response = sessionService.createSession(request);
        return ApiResponse.success(response);
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/{sessionId}")
    public ApiResponse<ChatSession> getSession(@PathVariable String sessionId) {
        ChatSession session = sessionService.getSession(sessionId);
        if (session == null) {
            return ApiResponse.error(404, "会话不存在");
        }
        return ApiResponse.success(session);
    }

    /**
     * 结束会话
     */
    @PostMapping("/{sessionId}/close")
    public ApiResponse<Void> closeSession(@PathVariable String sessionId) {
        ChatSession session = sessionService.getSession(sessionId);
        if (session == null) {
            return ApiResponse.error(404, "会话不存在");
        }

        // 创建工单
        workOrderService.createWorkOrder(session, "");

        sessionService.closeSession(sessionId);
        return ApiResponse.success("会话已关闭", null);
    }

    /**
     * 转接会话
     */
    @PostMapping("/{sessionId}/transfer")
    public ApiResponse<SessionResponse> transferSession(
            @PathVariable String sessionId,
            @RequestParam String targetAgentId) {
        try {
            SessionResponse response = sessionService.transferSession(sessionId, targetAgentId);
            return ApiResponse.success(response);
        } catch (IllegalStateException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取在线坐席列表
     */
    @GetMapping("/agents")
    public ApiResponse<List<Map<String, Object>>> getOnlineAgents(
            com.multica.customer.service.AgentService agentService) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Agent agent : agentService.getOnlineAgents().values()) {
            Map<String, Object> item = new HashMap<>();
            item.put("agentId", agent.getAgentId());
            item.put("name", agent.getName());
            item.put("status", agent.getStatus().name());
            item.put("currentLoad", agent.getCurrentLoad());
            result.add(item);
        }
        return ApiResponse.success(result);
    }
}
