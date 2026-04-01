package com.logarithm.microtask.entity;

import com.logarithm.microtask.entity.enums.ApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "applications",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_application_task_seller", columnNames = {"task_id", "seller_id"})
    },
    indexes = {
        @Index(name = "idx_application_task", columnList = "task_id"),
        @Index(name = "idx_application_seller", columnList = "seller_id")
    }
)
public class Application extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    @ToString.Exclude
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    @ToString.Exclude
    private User seller;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal proposedAmount;

    @Column(nullable = false, length = 500)
    private String coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;
}
