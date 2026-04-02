package com.logarithm.microtask.dto.application;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationCreateRequest {

    @NotNull(message = "Task ID is required")
    @Positive(message = "Task ID must be greater than 0")
    private Long taskId;

    @NotNull(message = "Proposed amount is required")
    @DecimalMin(value = "0.01", message = "Proposed amount must be at least 0.01")
    private BigDecimal proposedAmount;

    @NotBlank(message = "Cover letter is required")
    @Size(max = 500, message = "Cover letter must be at most 500 characters")
    private String coverLetter;
}
