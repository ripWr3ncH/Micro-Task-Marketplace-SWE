package com.logarithm.microtask.service;

import com.logarithm.microtask.dto.application.ApplicationCreateRequest;
import com.logarithm.microtask.dto.application.ApplicationResponse;
import com.logarithm.microtask.dto.taskassignment.TaskAssignmentResponse;

import java.util.List;

public interface ApplicationService {
    ApplicationResponse applyToTask(ApplicationCreateRequest request, String sellerEmail);

    List<ApplicationResponse> getApplicationsByTask(Long taskId, String userEmail, boolean isAdmin);

    List<ApplicationResponse> getMyApplications(String sellerEmail);

    List<ApplicationResponse> getAllApplicationsForAdmin();

    TaskAssignmentResponse acceptApplication(Long applicationId, String userEmail, boolean isAdmin);
}
