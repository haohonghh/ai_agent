package com.multica.customer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * FAQ检索请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqSearchRequest {

    private String query;
    private String category;
    private Integer limit;
}
