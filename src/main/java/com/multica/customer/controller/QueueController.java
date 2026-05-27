package com.multica.customer.controller;

import com.multica.customer.dto.ApiResponse;
import com.multica.customer.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 队列控制器
 */
@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    /**
     * 获取当前排队人数
     */
    @GetMapping("/size")
    public ApiResponse<Map<String, Integer>> getQueueSize() {
        Map<String, Integer> result = new HashMap<>();
        result.put("size", queueService.getQueueSize());
        return ApiResponse.success(result);
    }

    /**
     * 获取用户在队列中的位置
     */
    @GetMapping("/position")
    public ApiResponse<Map<String, Object>> getQueuePosition(@RequestParam String sessionId) {
        int position = queueService.getQueuePosition(sessionId);
        if (position < 0) {
            return ApiResponse.error(404, "用户不在队列中");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("position", position);
        return ApiResponse.success(result);
    }
}
