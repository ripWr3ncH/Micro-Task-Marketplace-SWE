package com.logarithm.microtask.service.impl;

import com.logarithm.microtask.dto.task.TaskCreateRequest;
import com.logarithm.microtask.dto.task.TaskResponse;
import com.logarithm.microtask.dto.task.TaskUpdateRequest;
import com.logarithm.microtask.entity.Task;
import com.logarithm.microtask.entity.User;
import com.logarithm.microtask.entity.enums.TaskStatus;
import com.logarithm.microtask.exception.ForbiddenOperationException;
import com.logarithm.microtask.exception.ResourceNotFoundException;
import com.logarithm.microtask.repository.TaskRepository;
import com.logarithm.microtask.repository.UserRepository;
import com.logarithm.microtask.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    public TaskResponse createTask(TaskCreateRequest request, String userEmail) {
        User buyer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .budget(request.getBudget())
                .status(TaskStatus.OPEN)
                .buyer(buyer)
                .build();

        return mapToResponse(taskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        return mapToResponse(findTask(taskId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request, String userEmail, boolean isAdmin) {
        Task task = findTask(taskId);
        checkTaskOwner(task, userEmail, isAdmin);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getBudget() != null) {
            task.setBudget(request.getBudget());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        return mapToResponse(taskRepository.save(task));
    }

    @Override
    public void deleteTask(Long taskId, String userEmail, boolean isAdmin) {
        Task task = findTask(taskId);
        checkTaskOwner(task, userEmail, isAdmin);
        taskRepository.delete(task);
    }

    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    private void checkTaskOwner(Task task, String userEmail, boolean isAdmin) {
        if (!isAdmin && !task.getBuyer().getEmail().equalsIgnoreCase(userEmail)) {
            throw new ForbiddenOperationException("You are not allowed to modify this task.");
        }
    }

    private TaskResponse mapToResponse(Task task) {
        Long assignedSellerId = task.getAssignment() != null ? task.getAssignment().getSeller().getId() : null;
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .budget(task.getBudget())
                .status(task.getStatus())
                .buyerId(task.getBuyer().getId())
                .buyerName(task.getBuyer().getFullName())
                .assignedSellerId(assignedSellerId)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
