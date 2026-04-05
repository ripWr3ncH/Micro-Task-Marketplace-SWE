package com.logarithm.microtask.controller;

import com.logarithm.microtask.dto.admin.AdminUserResponse;
import com.logarithm.microtask.service.AdminUserManagementService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUserController {

    private final AdminUserManagementService adminUserManagementService;

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminUserManagementService.getAllUsers());
    }

    @PatchMapping("/{userId}/block")
    public ResponseEntity<AdminUserResponse> blockUser(
            @PathVariable @Positive(message = "User ID must be greater than 0") Long userId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminUserManagementService.blockUser(userId, authentication.getName()));
    }

    @PatchMapping("/{userId}/unblock")
    public ResponseEntity<AdminUserResponse> unblockUser(
            @PathVariable @Positive(message = "User ID must be greater than 0") Long userId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminUserManagementService.unblockUser(userId, authentication.getName()));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable @Positive(message = "User ID must be greater than 0") Long userId,
            Authentication authentication
    ) {
        adminUserManagementService.deleteUser(userId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
