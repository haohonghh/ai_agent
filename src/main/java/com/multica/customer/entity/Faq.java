package com.multica.customer.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FAQ实体 - 用于ES存储
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faq {

    private String id;
    private String question;
    private String answer;
    private String category;
    private List<String> keywords;
    private Integer hitCount;
    private Double confidenceThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
