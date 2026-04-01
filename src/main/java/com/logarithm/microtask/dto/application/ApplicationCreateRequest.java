package com.logarithm.microtask.dto.application;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Long taskId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal proposedAmount;

    @NotBlank
    @Size(max = 500)
    private String coverLetter;
}
