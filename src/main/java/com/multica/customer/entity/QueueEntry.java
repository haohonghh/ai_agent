package com.multica.customer.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 队列条目实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueEntry {

    private String queueId;
    private String sessionId;
    private Integer position;
    private QueuePriority priority;
    private LocalDateTime enqueuedAt;
    private LocalDateTime estimatedServeAt;
    private Integer estimatedWaitSeconds;
    private LocalDateTime timeoutAt;

    public enum QueuePriority {
        LOW(1),
        NORMAL(2),
        HIGH(3),
        VIP(4);

        private final int level;

        QueuePriority(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }
}
