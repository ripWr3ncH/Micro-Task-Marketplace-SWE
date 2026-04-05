package com.logarithm.microtask.service;

import com.logarithm.microtask.dto.admin.AdminUserResponse;

import java.util.List;

public interface AdminUserManagementService {
    List<AdminUserResponse> getAllUsers();

    AdminUserResponse blockUser(Long userId, String adminEmail);

    AdminUserResponse unblockUser(Long userId, String adminEmail);

    void deleteUser(Long userId, String adminEmail);
}
