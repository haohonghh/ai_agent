package com.multica.customer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 创建会话请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSessionRequest {

    private String userId;
    private String userName;
    private String source;
    private String initialMessage;
}
