package com.logarithm.microtask.dto.application;

import com.logarithm.microtask.entity.enums.ApplicationStatus;
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
public class ApplicationResponse {
    private Long id;
    private Long taskId;
    private Long sellerId;
    private String sellerName;
    private BigDecimal proposedAmount;
    private String coverLetter;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
}
