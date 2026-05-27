package com.smartcare.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateConversationRequest {
    private String source = "web";
    private Integer priority = 0;
    private String customerName;
}