package com.smartcare.controller;

import com.smartcare.dto.ApiResponse;
import com.smartcare.dto.SemanticSearchRequest;
import com.smartcare.model.entity.FaqKnowledge;
import com.smartcare.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @GetMapping("/search")
    public ApiResponse<Page<FaqKnowledge>> searchFaqs(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FaqKnowledge> results = faqService.searchByKeyword(keyword, categoryId, PageRequest.of(page, size));
        return ApiResponse.success(results);
    }

    @PostMapping("/semantic-search")
    public ApiResponse<List<Map<String, Object>>> semanticSearch(@RequestBody SemanticSearchRequest request) {
        List<Map<String, Object>> results = faqService.semanticSearch(request.getQuery(), request.getTopK());
        return ApiResponse.success(results);
    }

    @GetMapping("/{faqId}")
    public ApiResponse<FaqKnowledge> getFaq(@PathVariable Long faqId) {
        FaqKnowledge faq = faqService.getFaqById(faqId);
        if (faq != null) {
            faqService.incrementViewCount(faqId);
            return ApiResponse.success(faq);
        }
        return ApiResponse.error(40401, "FAQ不存在");
    }

    @PostMapping("/{faqId}/rate")
    public ApiResponse<Void> rateFaq(@PathVariable Long faqId, @RequestBody Map<String, Boolean> request) {
        Boolean helpful = request.get("helpful");
        if (helpful != null && helpful) {
            faqService.incrementHelpfulCount(faqId);
        }
        return ApiResponse.success();
    }
}