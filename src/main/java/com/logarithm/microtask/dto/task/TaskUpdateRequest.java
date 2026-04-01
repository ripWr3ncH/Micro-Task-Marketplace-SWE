package com.logarithm.microtask.dto.task;

import com.logarithm.microtask.entity.enums.TaskStatus;
import jakarta.validation.constraints.DecimalMin;
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
public class TaskUpdateRequest {

    @Size(max = 150)
    private String title;

    @Size(max = 2000)
    private String description;

    @DecimalMin(value = "0.01")
    private BigDecimal budget;

    private TaskStatus status;
}
