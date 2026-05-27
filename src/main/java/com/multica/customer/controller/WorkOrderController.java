package com.multica.customer.controller;

import com.multica.customer.dto.ApiResponse;
import com.multica.customer.dto.SatisfactionRequest;
import com.multica.customer.entity.WorkOrder;
import com.multica.customer.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 工单控制器
 */
@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    /**
     * 获取工单详情
     */
    @GetMapping("/{workOrderId}")
    public ApiResponse<WorkOrder> getWorkOrder(@PathVariable String workOrderId) {
        return workOrderService.getWorkOrder(workOrderId)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "工单不存在"));
    }

    /**
     * 提交满意度评价
     */
    @PostMapping("/{workOrderId}/satisfaction")
    public ApiResponse<WorkOrder> submitSatisfaction(
            @PathVariable String workOrderId,
            @RequestBody SatisfactionRequest request) {
        try {
            request.setWorkOrderId(workOrderId);
            WorkOrder workOrder = workOrderService.submitSatisfaction(request);
            return ApiResponse.success(workOrder);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 关闭工单
     */
    @PostMapping("/{workOrderId}/close")
    public ApiResponse<Void> closeWorkOrder(@PathVariable String workOrderId) {
        workOrderService.closeWorkOrder(workOrderId);
        return ApiResponse.success("工单已关闭", null);
    }
}
