package com.logarithm.microtask.service;

import com.logarithm.microtask.dto.task.TaskCreateRequest;
import com.logarithm.microtask.dto.task.TaskResponse;
import com.logarithm.microtask.dto.task.TaskUpdateRequest;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(TaskCreateRequest request, String userEmail);

    TaskResponse getTaskById(Long taskId);

    List<TaskResponse> getAllTasks();

    List<TaskResponse> getTasksByBuyer(String userEmail);

    TaskResponse updateTask(Long taskId, TaskUpdateRequest request, String userEmail, boolean isAdmin);

    void deleteTask(Long taskId, String userEmail, boolean isAdmin);
}
