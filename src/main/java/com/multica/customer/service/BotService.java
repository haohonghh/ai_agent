package com.multica.customer.service;

import com.multica.customer.entity.Faq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 机器人服务 - FAQ检索、意图识别、自动问答
 */
@Slf4j
@Service
public class BotService {

    private final List<Faq> faqCache = new ArrayList<>();

    public BotService() {
        initFaqData();
    }

    private void initFaqData() {
        // 初始化FAQ数据（实际应从ES加载）
        faqCache.add(Faq.builder()
                .id(UUID.randomUUID().toString())
                .question("如何重置密码")
                .answer("您可以通过以下步骤重置密码：1. 点击登录页的\"忘记密码\"链接；2. 输入注册邮箱；3. 查收邮件并点击验证链接；4. 设置新密码")
                .category("账户")
                .keywords(Arrays.asList("密码", "重置", "忘记"))
                .hitCount(0)
                .confidenceThreshold(0.7)
                .createdAt(LocalDateTime.now())
                .build());

        faqCache.add(Faq.builder()
                .id(UUID.randomUUID().toString())
                .question("如何联系人工客服")
                .answer("您可以输入\"转人工\"或\"人工客服\"来连接我们的坐席团队，我们的工作时间是周一至周五 9:00-18:00。")
                .category("服务")
                .keywords(Arrays.asList("人工", "客服", "转接"))
                .hitCount(0)
                .confidenceThreshold(0.7)
                .createdAt(LocalDateTime.now())
                .build());

        faqCache.add(Faq.builder()
                .id(UUID.randomUUID().toString())
                .question("业务办理时间")
                .answer("我们的业务办理时间为：周一至周五 9:00-18:00，周六 10:00-16:00，周日休息。")
                .category("服务")
                .keywords(Arrays.asList("时间", "营业", "办公"))
                .hitCount(0)
                .confidenceThreshold(0.7)
                .createdAt(LocalDateTime.now())
                .build());

        log.info("初始化 {} 条FAQ", faqCache.size());
    }

    /**
     * 语义检索FAQ
     */
    public Faq searchFaq(String query) {
        if (query == null || query.trim().isEmpty()) {
            return null;
        }

        String lowerQuery = query.toLowerCase();

        // 简单的关键词匹配（实际应使用ES的语义匹配）
        for (Faq faq : faqCache) {
            if (faq.getQuestion().toLowerCase().contains(lowerQuery)) {
                faq.setHitCount(faq.getHitCount() + 1);
                return faq;
            }

            for (String keyword : faq.getKeywords()) {
                if (lowerQuery.contains(keyword.toLowerCase())) {
                    faq.setHitCount(faq.getHitCount() + 1);
                    return faq;
                }
            }
        }

        // 未找到匹配
        return null;
    }

    /**
     * 意图识别
     */
    public String recognizeIntent(String query) {
        if (query == null) {
            return "unknown";
        }

        String lowerQuery = query.toLowerCase();

        // 转人工
        if (lowerQuery.contains("转人工") || lowerQuery.contains("人工") ||
                lowerQuery.contains("客服") || lowerQuery.contains("真人")) {
            return "transfer_to_agent";
        }

        // 投诉
        if (lowerQuery.contains("投诉")) {
            return "complaint";
        }

        // 咨询
        if (lowerQuery.contains("咨询") || lowerQuery.contains("请问")) {
            return "inquiry";
        }

        // 满意度评价
        if (lowerQuery.contains("满意") || lowerQuery.contains("评价")) {
            return "satisfaction";
        }

        return "general";
    }

    /**
     * 生成自动回复
     */
    public String generateAutoReply(String query) {
        Faq faq = searchFaq(query);
        if (faq != null) {
            return faq.getAnswer();
        }

        // 根据意图生成回复
        String intent = recognizeIntent(query);
        switch (intent) {
            case "transfer_to_agent":
                return "好的，我为您转接人工客服，请稍候...";
            case "complaint":
                return "非常抱歉给您带来不便，请您详细描述您遇到的问题，我们会尽快处理。";
            case "satisfaction":
                return "感谢您的反馈，请选择满意度评分（1-5星）：";
            default:
                return "抱歉，我无法理解您的问题。您可以：1. 尝试使用其他关键词；2. 输入\"转人工\"连接人工客服。";
        }
    }
}
