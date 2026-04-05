package com.logarithm.microtask.config;

import com.logarithm.microtask.entity.Role;
import com.logarithm.microtask.entity.User;
import com.logarithm.microtask.entity.enums.RoleName;
import com.logarithm.microtask.repository.RoleRepository;
import com.logarithm.microtask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean adminBootstrapEnabled;

    @Value("${app.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:}")
    private String adminPassword;

    @Value("${app.bootstrap.admin.full-name:System Admin}")
    private String adminFullName;

    @Override
    @Transactional
    public void run(String... args) {
        if (!adminBootstrapEnabled) {
            return;
        }

        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.warn("Admin bootstrap is enabled but email/password is missing. Skipping admin bootstrap.");
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ADMIN).build()));

        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseGet(() -> User.builder()
                        .fullName(adminFullName)
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .roles(new HashSet<>())
                        .build());

        if (adminUser.getRoles() == null) {
            adminUser.setRoles(new HashSet<>());
        }

        adminUser.getRoles().add(adminRole);
        adminUser.setPassword(passwordEncoder.encode(adminPassword));

        if (adminUser.getFullName() == null || adminUser.getFullName().isBlank()) {
            adminUser.setFullName(adminFullName);
        }

        userRepository.save(adminUser);
        log.info("Admin bootstrap ensured for {}", adminEmail);
    }
}
