package com.multica.customer.dto;

import com.multica.customer.entity.ChatSession;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 会话响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {

    private String sessionId;
    private ChatSession.SessionStatus status;
    private String agentId;
    private String agentName;
    private Integer queuePosition;
    private Long waitTimeSeconds;
    private String message;

    public static SessionResponse waiting(String sessionId, Integer queuePosition) {
        return SessionResponse.builder()
                .sessionId(sessionId)
                .status(ChatSession.SessionStatus.WAITING)
                .queuePosition(queuePosition)
                .message("正在为您连接坐席，请稍候...")
                .build();
    }

    public static SessionResponse connected(String sessionId, String agentId, String agentName) {
        return SessionResponse.builder()
                .sessionId(sessionId)
                .status(ChatSession.SessionStatus.CONNECTED)
                .agentId(agentId)
                .agentName(agentName)
                .message("已为您连接坐席 " + agentName)
                .build();
    }

    public static SessionResponse queued(String sessionId, Integer position, Long estimatedWait) {
        return SessionResponse.builder()
                .sessionId(sessionId)
                .status(ChatSession.SessionStatus.QUEUED)
                .queuePosition(position)
                .waitTimeSeconds(estimatedWait)
                .message("当前排队人数：" + position + "，预计等待：" + estimatedWait + "秒")
                .build();
    }
}
