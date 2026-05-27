package com.multica.customer.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 工单实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrder {

    private String workOrderId;
    private String sessionId;
    private String agentId;
    private String userId;
    private WorkOrderStatus status;
    private Integer satisfactionScore;
    private String satisfactionComment;
    private String summary;
    private String transcript;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;

    public enum WorkOrderStatus {
        OPEN,         // 开启
        IN_PROGRESS,  // 处理中
        RESOLVED,     // 已解决
        CLOSED,       // 已关闭
        CANCELLED     // 已取消
    }
}
