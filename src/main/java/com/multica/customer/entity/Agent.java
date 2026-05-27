package com.multica.customer.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 坐席实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agent {

    private String agentId;
    private String name;
    private AgentStatus status;
    private String skills;
    private Integer currentLoad;
    private Integer maxLoad;
    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;

    public enum AgentStatus {
        ONLINE,    // 在线
        BUSY,      // 忙碌
        AWAY,      // 离开
        OFFLINE    // 离线
    }
}
