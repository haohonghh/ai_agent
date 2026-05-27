package com.multica.customer.service;

import com.multica.customer.dto.SatisfactionRequest;
import com.multica.customer.entity.ChatSession;
import com.multica.customer.entity.WorkOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工单服务 - 会话记录、满意度评价
 */
@Slf4j
@Service
public class WorkOrderService {

    private final Map<String, WorkOrder> workOrders = new ConcurrentHashMap<>();

    /**
     * 创建工单
     */
    public WorkOrder createWorkOrder(ChatSession session, String transcript) {
        String workOrderId = "WO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        WorkOrder workOrder = WorkOrder.builder()
                .workOrderId(workOrderId)
                .sessionId(session.getSessionId())
                .agentId(session.getAgentId())
                .userId(session.getUserId())
                .status(WorkOrder.WorkOrderStatus.OPEN)
                .summary(generateSummary(session))
                .transcript(transcript)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        workOrders.put(workOrderId, workOrder);
        log.info("创建工单: {}, 会话: {}", workOrderId, session.getSessionId());

        return workOrder;
    }

    /**
     * 关闭工单
     */
    public void closeWorkOrder(String workOrderId) {
        WorkOrder workOrder = workOrders.get(workOrderId);
        if (workOrder == null) {
            return;
        }

        workOrder.setStatus(WorkOrder.WorkOrderStatus.CLOSED);
        workOrder.setClosedAt(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());

        log.info("工单已关闭: {}", workOrderId);
    }

    /**
     * 提交满意度评价
     */
    public WorkOrder submitSatisfaction(SatisfactionRequest request) {
        WorkOrder workOrder = workOrders.get(request.getWorkOrderId());
        if (workOrder == null) {
            throw new IllegalArgumentException("工单不存在: " + request.getWorkOrderId());
        }

        if (request.getScore() < 1 || request.getScore() > 5) {
            throw new IllegalArgumentException("评分必须在1-5之间");
        }

        workOrder.setSatisfactionScore(request.getScore());
        workOrder.setSatisfactionComment(request.getComment());
        workOrder.setStatus(WorkOrder.WorkOrderStatus.RESOLVED);
        workOrder.setUpdatedAt(LocalDateTime.now());

        log.info("工单 {} 满意度评分: {}", request.getWorkOrderId(), request.getScore());

        return workOrder;
    }

    /**
     * 获取工单
     */
    public Optional<WorkOrder> getWorkOrder(String workOrderId) {
        return Optional.ofNullable(workOrders.get(workOrderId));
    }

    /**
     * 获取会话的工单
     */
    public Optional<WorkOrder> getWorkOrderBySession(String sessionId) {
        return workOrders.values().stream()
                .filter(wo -> wo.getSessionId().equals(sessionId))
                .findFirst();
    }

    /**
     * 生成会话摘要
     */
    private String generateSummary(ChatSession session) {
        return String.format("%s 的客服会话 - %s",
                session.getUserName(),
                session.getCreatedAt().toLocalDate().toString());
    }
}
