package com.logarithm.microtask.service.impl;

import com.logarithm.microtask.dto.application.ApplicationCreateRequest;
import com.logarithm.microtask.dto.application.ApplicationResponse;
import com.logarithm.microtask.dto.taskassignment.TaskAssignmentResponse;
import com.logarithm.microtask.entity.Application;
import com.logarithm.microtask.entity.Task;
import com.logarithm.microtask.entity.TaskAssignment;
import com.logarithm.microtask.entity.User;
import com.logarithm.microtask.entity.enums.ApplicationStatus;
import com.logarithm.microtask.entity.enums.RoleName;
import com.logarithm.microtask.entity.enums.TaskStatus;
import com.logarithm.microtask.exception.BadRequestException;
import com.logarithm.microtask.exception.ForbiddenOperationException;
import com.logarithm.microtask.exception.ResourceNotFoundException;
import com.logarithm.microtask.repository.ApplicationRepository;
import com.logarithm.microtask.repository.TaskAssignmentRepository;
import com.logarithm.microtask.repository.TaskRepository;
import com.logarithm.microtask.repository.UserRepository;
import com.logarithm.microtask.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;

    @Override
    public ApplicationResponse applyToTask(ApplicationCreateRequest request, String sellerEmail) {
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));

        if (task.getStatus() != TaskStatus.OPEN) {
            throw new BadRequestException("Only OPEN tasks can receive applications.");
        }

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + sellerEmail));

        boolean hasSellerRole = seller.getRoles().stream()
            .anyMatch(role -> role.getName() == RoleName.SELLER);
        if (!hasSellerRole) {
            throw new ForbiddenOperationException("Only SELLER accounts can apply to tasks.");
        }

        if (applicationRepository.existsByTaskIdAndSellerId(task.getId(), seller.getId())) {
            throw new BadRequestException("You have already applied to this task.");
        }

        Application application = Application.builder()
                .task(task)
                .seller(seller)
                .proposedAmount(request.getProposedAmount())
                .coverLetter(request.getCoverLetter())
                .status(ApplicationStatus.PENDING)
                .build();

        return mapToResponse(applicationRepository.save(application));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByTask(Long taskId, String userEmail, boolean isAdmin) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (!isAdmin && !task.getBuyer().getEmail().equalsIgnoreCase(userEmail)) {
            throw new ForbiddenOperationException("You are not allowed to view applications for this task.");
        }

        return applicationRepository.findByTaskId(taskId).stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + sellerEmail));

        boolean hasSellerRole = seller.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.SELLER);
        if (!hasSellerRole) {
            throw new ForbiddenOperationException("Only SELLER accounts can view their applications.");
        }

        return applicationRepository.findBySellerId(seller.getId()).stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplicationsForAdmin() {
        return applicationRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public TaskAssignmentResponse acceptApplication(Long applicationId, String userEmail, boolean isAdmin) {
        Application selectedApplication = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        Task task = selectedApplication.getTask();
        if (!isAdmin && !task.getBuyer().getEmail().equalsIgnoreCase(userEmail)) {
            throw new ForbiddenOperationException("You are not allowed to accept applications for this task.");
        }

        if (taskAssignmentRepository.existsByTaskId(task.getId())) {
            throw new BadRequestException("This task is already assigned.");
        }

        List<Application> allTaskApplications = applicationRepository.findByTaskId(task.getId());
        for (Application application : allTaskApplications) {
            if (application.getId().equals(applicationId)) {
                application.setStatus(ApplicationStatus.ACCEPTED);
            } else {
                application.setStatus(ApplicationStatus.REJECTED);
            }
        }
        applicationRepository.saveAll(allTaskApplications);

        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);

        TaskAssignment taskAssignment = TaskAssignment.builder()
                .task(task)
                .seller(selectedApplication.getSeller())
                .build();
        TaskAssignment savedAssignment = taskAssignmentRepository.save(taskAssignment);

        return TaskAssignmentResponse.builder()
                .id(savedAssignment.getId())
                .taskId(task.getId())
                .sellerId(savedAssignment.getSeller().getId())
                .sellerName(savedAssignment.getSeller().getFullName())
                .createdAt(savedAssignment.getCreatedAt())
                .build();
    }

    private ApplicationResponse mapToResponse(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .taskId(application.getTask().getId())
                .sellerId(application.getSeller().getId())
                .sellerName(application.getSeller().getFullName())
                .proposedAmount(application.getProposedAmount())
                .coverLetter(application.getCoverLetter())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .build();
    }
}
