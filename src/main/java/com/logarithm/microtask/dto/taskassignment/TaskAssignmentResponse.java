package com.logarithm.microtask.dto.taskassignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignmentResponse {
    private Long id;
    private Long taskId;
    private Long sellerId;
    private String sellerName;
    private LocalDateTime createdAt;
}
