package com.smartcare.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "agent_id")
    private Long agentId;

    @Column(nullable = false, length = 20)
    private String status = "waiting";

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(nullable = false, length = 20)
    private String source = "web";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}