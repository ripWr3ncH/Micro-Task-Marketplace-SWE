package com.logarithm.microtask.service.impl;

import com.logarithm.microtask.dto.admin.AdminUserResponse;
import com.logarithm.microtask.entity.User;
import com.logarithm.microtask.entity.enums.RoleName;
import com.logarithm.microtask.exception.BadRequestException;
import com.logarithm.microtask.exception.ForbiddenOperationException;
import com.logarithm.microtask.exception.ResourceNotFoundException;
import com.logarithm.microtask.repository.ApplicationRepository;
import com.logarithm.microtask.repository.TaskAssignmentRepository;
import com.logarithm.microtask.repository.TaskRepository;
import com.logarithm.microtask.repository.UserRepository;
import com.logarithm.microtask.service.AdminUserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserManagementServiceImpl implements AdminUserManagementService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ApplicationRepository applicationRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public AdminUserResponse blockUser(Long userId, String adminEmail) {
        User user = findUser(userId);
        ensureManageableTarget(user, adminEmail);
        user.setBlocked(true);
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public AdminUserResponse unblockUser(Long userId, String adminEmail) {
        User user = findUser(userId);
        ensureManageableTarget(user, adminEmail);
        user.setBlocked(false);
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long userId, String adminEmail) {
        User user = findUser(userId);
        ensureManageableTarget(user, adminEmail);

        long activeAssignments = taskAssignmentRepository.countBySellerId(userId);
        if (activeAssignments > 0) {
            throw new BadRequestException("Cannot delete user with active task assignments.");
        }

        applicationRepository.deleteBySellerId(userId);
        taskRepository.deleteByBuyerId(userId);
        userRepository.delete(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private void ensureManageableTarget(User target, String adminEmail) {
        if (target.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new BadRequestException("You cannot manage your own admin account.");
        }
        if (hasRole(target, RoleName.ADMIN)) {
            throw new ForbiddenOperationException("Managing ADMIN accounts is not allowed.");
        }
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream().anyMatch(role -> role.getName() == roleName);
    }

    private AdminUserResponse mapToResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return AdminUserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .blocked(user.isBlocked())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
