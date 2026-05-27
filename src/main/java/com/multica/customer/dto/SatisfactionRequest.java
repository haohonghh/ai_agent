package com.multica.customer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 满意度评价请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SatisfactionRequest {

    private String workOrderId;
    private Integer score;
    private String comment;
}
