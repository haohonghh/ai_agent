package com.multica.customer.controller;

import com.multica.customer.dto.ApiResponse;
import com.multica.customer.dto.FaqSearchRequest;
import com.multica.customer.entity.Faq;
import com.multica.customer.service.BotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 机器人控制器
 */
@RestController
@RequestMapping("/api/bot")
@RequiredArgsConstructor
public class BotController {

    private final BotService botService;

    /**
     * 检索FAQ
     */
    @PostMapping("/search")
    public ApiResponse<Faq> searchFaq(@RequestBody FaqSearchRequest request) {
        Faq faq = botService.searchFaq(request.getQuery());
        if (faq == null) {
            return ApiResponse.success("未找到匹配的FAQ", null);
        }
        return ApiResponse.success(faq);
    }

    /**
     * 获取自动回复
     */
    @GetMapping("/reply")
    public ApiResponse<String> getReply(@RequestParam String query) {
        String reply = botService.generateAutoReply(query);
        return ApiResponse.success(reply);
    }

    /**
     * 意图识别
     */
    @GetMapping("/intent")
    public ApiResponse<String> recognizeIntent(@RequestParam String query) {
        String intent = botService.recognizeIntent(query);
        return ApiResponse.success(intent);
    }
}
