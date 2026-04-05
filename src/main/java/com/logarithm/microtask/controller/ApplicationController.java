package com.logarithm.microtask.controller;

import com.logarithm.microtask.dto.application.ApplicationCreateRequest;
import com.logarithm.microtask.dto.application.ApplicationResponse;
import com.logarithm.microtask.dto.taskassignment.TaskAssignmentResponse;
import com.logarithm.microtask.service.ApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Validated
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponse> apply(@Valid @RequestBody ApplicationCreateRequest request,
                                                     Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.applyToTask(request, authentication.getName()));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByTask(@PathVariable @Positive(message = "Task ID must be greater than 0") Long taskId,
                                                                           Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(applicationService.getApplicationsByTask(taskId, authentication.getName(), isAdmin));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(Authentication authentication) {
        return ResponseEntity.ok(applicationService.getMyApplications(authentication.getName()));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<ApplicationResponse>> getAllApplicationsForAdmin() {
        return ResponseEntity.ok(applicationService.getAllApplicationsForAdmin());
    }

    @PostMapping("/{applicationId}/accept")
    public ResponseEntity<TaskAssignmentResponse> acceptApplication(
            @PathVariable @Positive(message = "Application ID must be greater than 0") Long applicationId,
                                                                    Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(applicationService.acceptApplication(applicationId, authentication.getName(), isAdmin));
    }
}
