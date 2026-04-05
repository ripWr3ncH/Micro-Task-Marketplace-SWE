package com.logarithm.microtask.service.impl;

import com.logarithm.microtask.dto.application.ApplicationCreateRequest;
import com.logarithm.microtask.entity.Application;
import com.logarithm.microtask.entity.Role;
import com.logarithm.microtask.entity.Task;
import com.logarithm.microtask.entity.TaskAssignment;
import com.logarithm.microtask.entity.User;
import com.logarithm.microtask.entity.enums.ApplicationStatus;
import com.logarithm.microtask.entity.enums.RoleName;
import com.logarithm.microtask.entity.enums.TaskStatus;
import com.logarithm.microtask.exception.BadRequestException;
import com.logarithm.microtask.exception.ForbiddenOperationException;
import com.logarithm.microtask.repository.ApplicationRepository;
import com.logarithm.microtask.repository.TaskAssignmentRepository;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskAssignmentRepository taskAssignmentRepository;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private User buyer;
    private User seller;
    private Task task;

    @BeforeEach
    void setUp() {
        buyer = User.builder()
            .email("buyer@test.com")
            .fullName("Buyer")
            .password("x")
            .roles(Set.of(Role.builder().name(RoleName.BUYER).build()))
            .build();
        buyer.setId(1L);
        seller = User.builder()
            .email("seller@test.com")
            .fullName("Seller")
            .password("x")
            .roles(Set.of(Role.builder().name(RoleName.SELLER).build()))
            .build();
        seller.setId(2L);

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
    void applyToTaskShouldSucceed() {
        ApplicationCreateRequest request = ApplicationCreateRequest.builder()
                .taskId(10L)
                .proposedAmount(BigDecimal.valueOf(8))
                .coverLetter("I can do this")
                .build();

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(applicationRepository.existsByTaskIdAndSellerId(10L, 2L)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(100L);
            return a;
        });

        var response = applicationService.applyToTask(request, "seller@test.com");

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void applyToTaskShouldFailForNonOpenTask() {
        task.setStatus(TaskStatus.COMPLETED);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> applicationService.applyToTask(ApplicationCreateRequest.builder().taskId(10L).build(), "seller@test.com"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void applyToTaskShouldFailWhenDuplicate() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(applicationRepository.existsByTaskIdAndSellerId(10L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.applyToTask(ApplicationCreateRequest.builder().taskId(10L).build(), "seller@test.com"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getApplicationsByTaskShouldReturnList() {
        Application app = Application.builder().task(task).seller(seller).status(ApplicationStatus.PENDING).proposedAmount(BigDecimal.ONE).coverLetter("x").build();
        app.setId(44L);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(applicationRepository.findByTaskId(10L)).thenReturn(List.of(app));

        var responses = applicationService.getApplicationsByTask(10L, "buyer@test.com", false);

        assertThat(responses).hasSize(1);
    }

    @Test
    void getApplicationsByTaskShouldFailForNonOwner() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> applicationService.getApplicationsByTask(10L, "other@test.com", false))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getMyApplicationsShouldReturnSellerApplications() {
        Application app = Application.builder()
                .task(task)
                .seller(seller)
                .status(ApplicationStatus.PENDING)
                .proposedAmount(BigDecimal.ONE)
                .coverLetter("x")
                .build();
        app.setId(99L);

        when(userRepository.findByEmail("seller@test.com")).thenReturn(Optional.of(seller));
        when(applicationRepository.findBySellerId(2L)).thenReturn(List.of(app));

        var responses = applicationService.getMyApplications("seller@test.com");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getSellerId()).isEqualTo(2L);
    }

    @Test
    void getMyApplicationsShouldFailForNonSeller() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(buyer));

        assertThatThrownBy(() -> applicationService.getMyApplications("buyer@test.com"))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getAllApplicationsForAdminShouldReturnList() {
        Application app = Application.builder()
                .task(task)
                .seller(seller)
                .status(ApplicationStatus.PENDING)
                .proposedAmount(BigDecimal.ONE)
                .coverLetter("x")
                .build();
        app.setId(77L);

        when(applicationRepository.findAll()).thenReturn(List.of(app));

        var responses = applicationService.getAllApplicationsForAdmin();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(77L);
    }

    @Test
    void acceptApplicationShouldSucceed() {
        Application selected = Application.builder().task(task).seller(seller).status(ApplicationStatus.PENDING).proposedAmount(BigDecimal.ONE).coverLetter("x").build();
        selected.setId(50L);

        Application other = Application.builder().task(task).seller(User.builder().email("other@test.com").fullName("Other").password("x").build()).status(ApplicationStatus.PENDING).proposedAmount(BigDecimal.ONE).coverLetter("y").build();
        other.setId(51L);

        when(applicationRepository.findById(50L)).thenReturn(Optional.of(selected));
        when(taskAssignmentRepository.existsByTaskId(10L)).thenReturn(false);
        when(applicationRepository.findByTaskId(10L)).thenReturn(List.of(selected, other));
        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenAnswer(inv -> {
            TaskAssignment ta = inv.getArgument(0);
            ta.setId(200L);
            return ta;
        });

        var response = applicationService.acceptApplication(50L, "buyer@test.com", false);

        assertThat(response.getId()).isEqualTo(200L);
        assertThat(selected.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        assertThat(other.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(applicationRepository).saveAll(any());
        verify(taskRepository).save(task);
    }

    @Test
    void acceptApplicationShouldFailForNonOwner() {
        Application selected = Application.builder().task(task).seller(seller).status(ApplicationStatus.PENDING).proposedAmount(BigDecimal.ONE).coverLetter("x").build();
        selected.setId(50L);
        when(applicationRepository.findById(50L)).thenReturn(Optional.of(selected));

        assertThatThrownBy(() -> applicationService.acceptApplication(50L, "other@test.com", false))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void acceptApplicationShouldFailWhenAlreadyAssigned() {
        Application selected = Application.builder().task(task).seller(seller).status(ApplicationStatus.PENDING).proposedAmount(BigDecimal.ONE).coverLetter("x").build();
        selected.setId(50L);
        when(applicationRepository.findById(50L)).thenReturn(Optional.of(selected));
        when(taskAssignmentRepository.existsByTaskId(10L)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.acceptApplication(50L, "buyer@test.com", false))
                .isInstanceOf(BadRequestException.class);
    }
}
