package com.smartcare.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class SemanticSearchRequest {
    @NotBlank(message = "查询内容不能为空")
    private String query;
    private int topK = 5;
}