package com.multica.customer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * WebSocket消息传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMessage {

    private String type;
    private String sessionId;
    private String content;
    private String senderId;
    private String senderType;
    private Object data;
    private Long timestamp;
}
