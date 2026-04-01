package com.logarithm.microtask.repository;

import com.logarithm.microtask.entity.Application;
import com.logarithm.microtask.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByTaskId(Long taskId);

    List<Application> findBySellerId(Long sellerId);

    boolean existsByTaskIdAndSellerId(Long taskId, Long sellerId);

    List<Application> findByTaskIdAndStatus(Long taskId, ApplicationStatus status);
}
