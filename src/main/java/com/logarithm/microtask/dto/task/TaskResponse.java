package com.logarithm.microtask.dto.task;

import com.logarithm.microtask.entity.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal budget;
    private TaskStatus status;
    private Long buyerId;
    private String buyerName;
    private Long assignedSellerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
