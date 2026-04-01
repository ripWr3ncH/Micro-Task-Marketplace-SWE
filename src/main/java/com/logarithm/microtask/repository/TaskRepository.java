package com.logarithm.microtask.repository;

import com.logarithm.microtask.entity.Task;
import com.logarithm.microtask.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByBuyerId(Long buyerId);

    List<Task> findByStatus(TaskStatus status);
}
