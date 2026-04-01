package com.logarithm.microtask.repository;

import com.logarithm.microtask.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
    Optional<TaskAssignment> findByTaskId(Long taskId);

    boolean existsByTaskId(Long taskId);
}
