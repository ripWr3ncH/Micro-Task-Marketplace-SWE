package com.logarithm.microtask.service.impl;

import com.logarithm.microtask.dto.task.TaskCreateRequest;
import com.logarithm.microtask.dto.task.TaskUpdateRequest;
import com.logarithm.microtask.entity.Task;
import com.logarithm.microtask.entity.User;
import com.logarithm.microtask.entity.enums.TaskStatus;
import com.logarithm.microtask.exception.ForbiddenOperationException;
import com.logarithm.microtask.exception.ResourceNotFoundException;
import com.logarithm.microtask.repository.TaskRepository;
import com.logarithm.microtask.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User buyer;
    private Task task;

    @BeforeEach
    void setUp() {
        buyer = User.builder().fullName("Buyer").email("buyer@test.com").password("x").build();
        buyer.setId(1L);

        task = Task.builder()
                .title("Task")
                .description("Desc")
                .budget(BigDecimal.TEN)
                .status(TaskStatus.OPEN)
                .buyer(buyer)
                .build();
        task.setId(10L);
    }

    @Test
    void createTaskShouldSucceed() {
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("New")
                .description("New Desc")
                .budget(BigDecimal.valueOf(20))
                .build();

        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task saved = inv.getArgument(0);
            saved.setId(20L);
            return saved;
        });

        var response = taskService.createTask(request, "buyer@test.com");

        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.OPEN);
    }

    @Test
    void createTaskShouldThrowWhenUserMissing() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(TaskCreateRequest.builder().build(), "missing@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTaskByIdShouldReturnTask() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        var response = taskService.getTaskById(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getBuyerName()).isEqualTo("Buyer");
    }

    @Test
    void updateTaskShouldSucceedForOwner() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskUpdateRequest request = TaskUpdateRequest.builder().title("Updated").build();
        var response = taskService.updateTask(10L, request, "buyer@test.com", false);

        assertThat(response.getTitle()).isEqualTo("Updated");
    }

    @Test
    void updateTaskShouldThrowForNonOwner() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.updateTask(10L, TaskUpdateRequest.builder().title("X").build(), "other@test.com", false))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void updateTaskShouldAllowAdminForNonOwner() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskUpdateRequest request = TaskUpdateRequest.builder().title("Admin Updated").build();
        var response = taskService.updateTask(10L, request, "admin@test.com", true);

        assertThat(response.getTitle()).isEqualTo("Admin Updated");
    }

    @Test
    void deleteTaskShouldAllowAdmin() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        taskService.deleteTask(10L, "admin@test.com", true);

        verify(taskRepository).delete(task);
    }

    @Test
    void getAllTasksShouldReturnList() {
        when(taskRepository.findAll()).thenReturn(List.of(task));

        var response = taskService.getAllTasks();

        assertThat(response).hasSize(1);
    }

    @Test
    void getTasksByBuyerShouldReturnList() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));
        when(taskRepository.findByBuyerId(1L)).thenReturn(List.of(task));

        var response = taskService.getTasksByBuyer("buyer@test.com");

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getBuyerId()).isEqualTo(1L);
    }
}
