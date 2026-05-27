package com.smartcare.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class SendMessageRequest {
    private String messageType = "text";
    @NotBlank(message = "消息内容不能为空")
    private String content;
}